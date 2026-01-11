package megalodonte.utils;

import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String FX_FONT_SIZE = "-fx-font-size";

    /**
     * Usado em Text
     */
    public static String FX_FILL = "-fx-fill";

    /**
     * Usado em Button
     */
    public static String FX_TEXT_FILL = "-fx-text-fill";
    /**
     * Usado em Button
     */
    public static String FX_BG_COLOR = "-fx-background-color";
    public static String FX_BG_RADIUS = "-fx-background-radius";
    public static String FX_BORDER_WIDTH = "-fx-border-width";
    public static String FX_BORDER_COLOR = "-fx-border-color";
    public static String FX_BORDER_RADIUS = "-fx-border-radius";
    public static String FX_FONT_WEIGHT = "-fx-font-weight";

    public static String FX_PLACEHOLDER_COLOR = "-fx-prompt-text-fill";


    public static void updateTextColor(Node node, String color) {
        modifyStyles(node, color, FX_FILL);
    }

    // INPUTS//

    public static void updatePlaceholderColor(Node node, String color) {
        modifyStyles(node, color, FX_PLACEHOLDER_COLOR);
    }

    public static void updateTextColor_Input(Node node, String color) {
        modifyStyles(node, color, FX_TEXT_FILL);
    }

    public static void updateFontSize(Node node, int value) {
        modifyStyles(node, String.valueOf(value), FX_FONT_SIZE);
    }

    /**BORDER **/

    public static void updateBorderColor(Node node, String color) {
        modifyStyles(node, color, FX_BORDER_COLOR);
    }

    public static void updateBorderWidth(Node node, int width) {
        modifyStyles(node, width + "px", FX_BORDER_WIDTH);
    }

    public static void updateBorderRadius(Node node, int radius) {
        modifyStyles(node, radius + "px", FX_BORDER_RADIUS);
        modifyStyles(node, radius + "px", FX_BG_RADIUS);
    }


    /**
     * Cam be used in Regions like VBox
     * @param node
     * @param color
     */
    public static void updateBackgroundColor(Node node, String color) {
        modifyStyles(node, color, FX_BG_COLOR);
    }

    //TODO: REMOVER
    private static void modifyStyles(Node node, String color, String fxField) {
        var current = node.getStyle();
        var updated = Utils.UpdateEspecificStyle(
                current,
                fxField,
                color
        );
        node.setStyle(updated);
    }


    //TODO: TORNAR ESSE MÉTODO PRIVADO
    public static String UpdateEspecificStyle(
            String currentStyle,
            String targetField,
            String value) {

        // Cria a string de estilo com o valor a ser atualizado
        String newStyle = targetField + ": " + value + ";";

        // Verifica se o estilo já contém o campo de destino
        if (currentStyle.contains(targetField)) {
            // Substitui a parte do estilo correspondente ao targetField com o novo valor
            currentStyle = currentStyle.replaceAll(
                    "(?i)" + targetField + ":\\s*[^;]+;", // Captura o campo de destino e o valor atual, ignorando
                    // espaços extras
                    newStyle); // Substitui com o novo valor
        } else {
            // Se não houver, adiciona o novo estilo no final
            if (!currentStyle.endsWith(" ")) { // Evita duplicação de espaços
                currentStyle += " ";
            }
            currentStyle += newStyle; // Adiciona o novo estilo ao final
        }

        return currentStyle;
    }

    public static String getValueOfSpecificField(
            String currentStyle,
            String targetField) {

        // Verifica se o campo está presente
        if (currentStyle.contains(targetField)) {
            // Expressão regular para capturar o valor do campo, tratando espaços extras e
            // valores de cor
            String regex = targetField + ":\\s*([^;]+);"; // \\s* permite espaços extras
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(currentStyle);

            // Se encontrar uma correspondência, retorna o valor
            if (matcher.find()) {
                return matcher.group(1); // grupo 1 contém o valor após ":"
            }
        }

        // Se não encontrar o campo, retorna uma string vazia
        return "";
    }

    public static String ColortoHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    static void main() {
        //getVariableNamesInDataTable().forEach(IO::println);
        // getValuesFromVariablename("colors").forEach(IO::println);
    }

}
