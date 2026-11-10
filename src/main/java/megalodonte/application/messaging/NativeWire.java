package megalodonte.application.messaging;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** One bounded UTF-8 JSON envelope per line; stdout must contain protocol only. */
final class NativeWire {
    static final int VERSION = 1;
    static final int MAX_FRAME = 1024 * 1024;
    private static final Gson JSON = new GsonBuilder().setStrictness(Strictness.STRICT).create();
    record Message(int version, long generation, long sequence, String type, JsonElement payload) {
        <T> T body(Class<T> type) { return JSON.fromJson(payload, type); }
    }
    private final Reader input;
    private final Writer output;
    private long sent;
    private long received;
    NativeWire(InputStream input, OutputStream output) {
        this.input = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        this.output = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
    }
    synchronized void send(long generation, String type, Object body) throws IOException {
        String frame = JSON.toJson(new Message(VERSION, generation, ++sent, type, JSON.toJsonTree(body)));
        if (frame.length() > MAX_FRAME) throw new IOException("Native message exceeds size limit");
        output.write(frame); output.write('\n'); output.flush();
    }
    Message read() throws IOException {
        var line = new StringBuilder();
        for (int character; (character = input.read()) != -1;) {
            if (character == '\n') {
                try {
                    Message message = JSON.fromJson(line.toString(), Message.class);
                    if (message == null || message.version != VERSION || message.generation < 1
                            || message.sequence != received + 1 || message.type == null || message.payload == null)
                        throw new IOException("Invalid native protocol version, sequence or envelope");
                    received = message.sequence;
                    return message;
                } catch (JsonParseException | IllegalStateException error) {
                    throw new IOException("Invalid native JSON message", error);
                }
            }
            if (line.length() >= MAX_FRAME) throw new IOException("Native message exceeds size limit");
            line.append((char) character);
        }
        if (!line.isEmpty()) throw new EOFException("Truncated native message");
        return null;
    }
}
