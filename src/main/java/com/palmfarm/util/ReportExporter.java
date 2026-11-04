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

    /** จัดข้อความให้อยู่กลาง */
    public static String center(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    /** format row ตาราง */
    public static String row(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.length; i++) {
            String v = values[i] == null ? "" : values[i];
            if (v.length() > widths[i] - 2) v = v.substring(0, widths[i] - 3) + ".";
            sb.append(String.format(" %-" + (widths[i] - 2) + "s |", v));
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
