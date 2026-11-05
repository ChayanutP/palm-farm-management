package com.palmfarm.util;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportExporter {

    private static final String LINE  = "-".repeat(90);
    private static final String DLINE = "=".repeat(90);
    private static final DateTimeFormatter DT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** header มาตรฐาน */
    public static StringBuilder header(String reportTitle) {
        StringBuilder sb = new StringBuilder();
        sb.append(DLINE).append("\n");
        sb.append(center("PALM FARM MANAGEMENT SYSTEM", 90)).append("\n");
        sb.append(center(reportTitle, 90)).append("\n");
        sb.append(center("Generated: " + LocalDateTime.now().format(DT), 90)).append("\n");
        sb.append(DLINE).append("\n");
        return sb;
    }

    /** footer มาตรฐาน */
    public static String footer(int totalRows) {
        return LINE + "\n" +
               String.format("Total Records: %d\n", totalRows) +
               DLINE + "\n" +
               center("Palm Farm Management System -- Authorized Document", 90) + "\n";
    }

    /** ส่วนสรุปท้ายรายงาน (ใช้แทน footer เมื่อรายงานต้องการสรุปแบบกำหนดเอง) */
    public static String summary(String... lines) {
        StringBuilder sb = new StringBuilder(DLINE).append("\n").append("Summary:\n");
        for (String l : lines) sb.append("- ").append(l).append("\n");
        sb.append(DLINE).append("\n");
        sb.append(center("Palm Farm Management System -- Authorized Document", 90)).append("\n");
        return sb.toString();
    }

    /** เปิด FileChooser แล้วบันทึก */
    public static boolean saveToFile(Window owner, String defaultName, String content) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Report");
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text File (*.txt)", "*.txt"));
        File file = fc.showSaveDialog(owner);
        if (file == null) return false;
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            bw.write(content);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * ความกว้างที่ "มองเห็นจริง" ในฟอนต์ monospace
     * สระบน/ล่าง และวรรณยุกต์ภาษาไทย (เช่น ิ ี ึ ื ุ ู ั ็ ่ ้ ๊ ๋ ์) เป็นตัวอักษรที่ไม่กินที่ จึงนับเป็น 0
     * ถ้านับด้วย String.length() เหมือนเดิม แถวที่มีข้อความไทยจะดูสั้นกว่าแถวอื่นและขอบตารางเยื้อง
     */
    public static int displayWidth(String text) {
        int w = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            w += charWidth(cp);
        }
        return w;
    }

    private static int charWidth(int cp) {
        int type = Character.getType(cp);
        if (type == Character.NON_SPACING_MARK || type == Character.ENCLOSING_MARK || type == Character.FORMAT) return 0;
        return 1;
    }

    /** ตัดข้อความให้ไม่เกิน max ช่องที่มองเห็น (ไม่ตัดสระ/วรรณยุกต์ออกจากพยัญชนะที่ยังเหลือ) */
    private static String truncate(String text, int max) {
        if (displayWidth(text) <= max) return text;
        StringBuilder sb = new StringBuilder();
        int w = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int cw = charWidth(cp);
            if (w + cw > max - 1) break;           // เหลือที่ไว้ใส่ "."
            sb.appendCodePoint(cp);
            w += cw;
            i += Character.charCount(cp);
        }
        return sb.append('.').toString();
    }

    /** จัดข้อความให้อยู่กลาง */
    public static String center(String text, int width) {
        int tw = displayWidth(text);
        if (tw >= width) return text;
        int pad = (width - tw) / 2;
        return " ".repeat(pad) + text;
    }

    /** format row ตาราง — เติมช่องว่างตามความกว้างที่มองเห็นจริง ขอบตารางจึงตรงทุกแถว */
    public static String row(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.length; i++) {
            int inner = widths[i] - 2;
            String v = truncate(values[i] == null ? "" : values[i], inner);
            sb.append(' ').append(v).append(" ".repeat(inner - displayWidth(v))).append(" |");
        }
        return sb.toString();
    }

    /** เส้น separator */
    public static String separator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) sb.append("-".repeat(w)).append("+");
        return sb.toString();
    }
}
