package com.squ1dd13.msd.decomp;

import java.util.regex.*;

import static com.squ1dd13.msd.decomp.Highlight.TextColor.*;

public class Highlight {
    private static class RGBColor {
        int r, g, b;

        RGBColor(int red, int green, int blue) {
            r = red;
            g = green;
            b = blue;
        }

        String colorString() {
            return "\033[38;2;" + r + ";" + g + ";" + b + "m";
        }
    };

    public enum TextColor {
        White(255, 255, 255),
        Green(100, 255, 100),
        Blue(100, 100, 255),
        Red(255, 100, 100),
        LightBlue(200, 255, 255),
        Gray(120, 120, 120),
        LightPink(255, 200, 200),
        Pink(255, 150, 200),
        Orange(255, 150, 0),
        BlueGreen(0, 220, 200),
        LightYellow(255, 255, 100),
        MidBlue(50, 200, 255);

        private final Highlight.RGBColor color;
        TextColor(int r, int g, int b) {
            color = new Highlight.RGBColor(r, g, b);
        }

        public String highlight(String s) {
            return toString() + s + LightBlue.toString();
        }

        @Override
        public String toString() {
            return color.colorString();
        }
    }

    private static String operatorRegex = "oper";

    public static void addOperator(String s) {
        if(operatorRegex.equals("oper")) {
            operatorRegex = Pattern.quote(s);
        } else {
            operatorRegex += '|' + Pattern.quote(s);
        }
    }

    public static String highlightLine(String line) {
        return line
            .replaceAll("switch\\(([^:]+)\\)", Pink.highlight("switch") + "(" + Green.highlight("$1") + ")")
            .replaceAll("([a-zA-Z_$][a-zA-Z\\d_$]*)\\.([a-zA-Z_$][a-zA-Z\\d_$]*)", MidBlue.highlight("$1") + "." + BlueGreen.highlight("$2"))
            .replaceAll("(?<=(?:if\\()|(?:while\\()|(?:and)|(?:or))(\\s*\\?[^(]+)", LightYellow.highlight("$1"))
            .replaceAll("(&?(?:(?:global)|(?:local))[^_]+_\\d+(?:_[A-Za-z_0-9]+)?)\\.([^(]+)", "$1." + BlueGreen.highlight("$2"))
            .replaceAll("((?:else )?if)\\(([^)]+)\\)", Pink.highlight("$1") + "($2)")
            .replaceAll("} else \\{", "} " + Pink.highlight("else") + " {")
            .replaceAll("while\\(([^)]+)\\)", Pink.highlight("while") + "($1)")
            .replaceAll("(&?local[^_]+_\\d+(?:_[A-Za-z_0-9]+)?)", Orange.highlight("$1"))
            .replaceAll("(&?global[^_]+_\\d+(?:_[A-Za-z_0-9]+)?)", Red.highlight("$1"))
            .replaceAll("(?<=(?:\\()|(?:, ))(-?[\\d.]+f?)", Green.highlight("$1"))
            .replaceAll("([^(]+)\\(", LightPink.highlight("$1") + "(")
            .replaceAll("\"(\\.|[^\"])*\"", MidBlue.highlight("$0"))
            .replaceAll("(\\s*)//([^\\n]*)", Gray.highlight("$1/*$2 */"))
            .replaceAll("proc_[^(]+", BlueGreen.highlight("$0"))
            .replaceAll("void", Orange.highlight("$0"))
            .replaceAll("[{}()]", LightBlue.highlight("$0"))
            .replaceAll("case ([^:]+):", Pink.highlight("case ") + Green.highlight("$1") + ":")
            .replaceAll("goto ([^;]+)", Pink.highlight("goto ") + Red.highlight("$1"))
            .replaceAll("^(label_[^:]+):", Red.highlight("$1") + ":")
            .replaceAll("(break|continue);", Pink.highlight("$1") + ";")
            .replaceAll("([^\\s]+)\\s(" + operatorRegex + ")\\s([^\\s)]+)", String.format("$1 $2 %s", Green.highlight("$3")))
            ;
    }
}
