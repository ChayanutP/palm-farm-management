package com.palmfarm.controller.resource;

import com.palmfarm.controller.AccountingController;
import com.palmfarm.controller.ResourceController;
import com.palmfarm.util.ReportExporter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * หน้าออกรายงานของฝ่ายจัดสรรทรัพยากร — รวมปุ่ม Export ทุกรายงานไว้ที่เดียว
 * ทุกปุ่มดึงข้อมูลใหม่จาก DB ทุกครั้งที่กด (ไม่อ่านจากตารางบนหน้าจอ)
 */
public class ReportViewController {

    @FXML private Label lblMsg;

    private final ResourceController   ctrl = new ResourceController();
    private final AccountingController acct = new AccountingController();

    // ───────────────── งานและการจัดสรร ─────────────────

    @FXML public void handleExportWorkOrder(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        for (var t : ctrl.getAllTasks()) {
            rows.add(new String[] {
                String.valueOf(ctrl.task().getTaskId(t)),
                ctrl.task().getDescription(t),
                ctrl.task().getPlantationName(t),
                ctrl.task().getDepartmentDisplay(t),
                ctrl.task().getCompleteStatusDisplay(t),
                ctrl.task().getOrderedStatusDisplay(t),
                ctrl.task().getDeadline(t)
            });
        }
        exportTable("Work Order Report / รายงานใบสั่งงาน", "work_order_report.txt",
            new int[] {8, 32, 22, 18, 16, 18, 14},
            new String[] {"Task ID", "Description", "Plantation", "Department", "Status", "Assigned", "Deadline"},
            rows, "ไม่มีข้อมูลใบสั่งงาน");
    }

    /** รายงานนี้แยกตารางตาม task จึงไม่ใช้ exportTable() */
    @FXML public void handleExportAllocation(ActionEvent e) {
        int[] widths = {8, 26, 22, 18, 22, 12};
        String[] headers = {"Task ID", "Description", "Resource Name", "Type", "Plantation", "Date"};
        StringBuilder sb = ReportExporter.header("Resource Allocation Report / รายงานการจัดสรรทรัพยากร");
        int total = 0;
        for (var t : ctrl.getAllTasks()) {
            if (!ctrl.task().isAllocated(t)) continue;
            var rls = ctrl.getReservationsByTask(ctrl.task().getTaskId(t));
            if (rls.isEmpty()) continue;
            sb.append(ReportExporter.separator(widths)).append("\n");
            sb.append(ReportExporter.row(headers, widths)).append("\n");
            sb.append(ReportExporter.separator(widths)).append("\n");
            for (var rl : rls) {
                String[] row = {
                    String.valueOf(ctrl.task().getTaskId(t)),
                    ctrl.task().getDescription(t),
                    ctrl.rl().getResourceName(rl),
                    ctrl.rl().getResourceType(rl),
                    ctrl.task().getPlantationName(t),
                    ctrl.rl().getDate(rl)
                };
                sb.append(ReportExporter.row(row, widths)).append("\n");
                total++;
            }
        }
        if (total == 0) { showError(emptyOr("ไม่มีข้อมูลการจัดสรรทรัพยากร")); return; }
        sb.append(ReportExporter.separator(widths)).append("\n");
        sb.append(ReportExporter.footer(total));
        save("resource_allocation_report.txt", sb.toString());
    }

    // ───────────────── ทรัพยากร ─────────────────

    @FXML public void handleExportVehicle(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        for (var r : ctrl.getResourcesByType("VEHICLE")) {
            rows.add(new String[] {
                String.valueOf(ctrl.resource().getResourceId(r)),
                ctrl.resource().getLicensePlate(r),
                ctrl.resource().getVehicleSize(r),
                String.valueOf(ctrl.resource().getSeatCount(r)),
                ctrl.resource().getStatus(r)
            });
        }
        exportTable("Vehicle Report / รายงานรถยนต์ทั้งหมด", "vehicle_report.txt",
            new int[] {6, 20, 24, 8, 14},
            new String[] {"ID", "ทะเบียน", "ขนาดรถ", "ที่นั่ง", "สถานะ"},
            rows, "ไม่มีข้อมูลรถ");
    }

    @FXML public void handleExportEquip(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        for (var r : ctrl.getResourcesByType("EQUIPMENT")) {
            rows.add(new String[] {
                String.valueOf(ctrl.resource().getResourceId(r)),
                ctrl.resource().getEquipId(r),
                ctrl.resource().getEquipmentName(r),
                ctrl.resource().getStatus(r)
            });
        }
        exportTable("Equipment Report / รายงานอุปกรณ์เครื่องมือทั้งหมด", "equipment_report.txt",
            new int[] {6, 16, 34, 14},
            new String[] {"ID", "รหัสอุปกรณ์", "ชื่ออุปกรณ์", "สถานะ"},
            rows, "ไม่มีข้อมูลอุปกรณ์");
    }



    // ───────────────── ข้อมูลหลัก ─────────────────

    @FXML public void handleExportPlantation(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        for (var p : ctrl.getAllPlantations()) {
            rows.add(new String[] {
                String.valueOf(ctrl.plantation().getPlantationId(p)),
                ctrl.plantation().getName(p),
                String.format("%.2f", ctrl.plantation().getAreaRai(p)),
                ctrl.plantation().getLocation(p)
            });
        }
        exportTable("Plantation Report / รายงานสวนปาล์มทั้งหมด", "plantation_report.txt",
            new int[] {6, 28, 14, 36},
            new String[] {"ID", "ชื่อสวน", "พื้นที่ (ไร่)", "ที่ตั้ง"},
            rows, "ไม่มีข้อมูลสวนปาล์ม");
    }

    @FXML public void handleExportEmployee(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        for (var emp : ctrl.getAllEmployees()) {
            rows.add(new String[] {
                ctrl.employee().getNationalId(emp),
                ctrl.employee().getFullName(emp),
                ctrl.employee().getDepartment(emp),
                ctrl.employee().getPosition(emp),
                ctrl.employee().getPhone(emp)
            });
        }
        exportTable("Employee Report / รายงานพนักงานทั้งหมด", "employee_report.txt",
            new int[] {15, 26, 18, 18, 14},
            new String[] {"บัตรประชาชน", "ชื่อ-สกุล", "ฝ่าย", "ตำแหน่ง", "โทรศัพท์"},
            rows, "ไม่มีข้อมูลพนักงาน");
    }

    // ───────────────── บัญชี ─────────────────

    @FXML public void handleExportBill(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        for (var b : acct.getRecordedBills()) {
            rows.add(new String[] {
                String.valueOf(acct.bill().getBillId(b)),
                acct.bill().getBillNumber(b),
                acct.bill().getSaleDate(b),
                acct.bill().getBuyerName(b),
                String.format("%.0f", acct.bill().getPalmQuantity(b)),
                String.format("%.2f", acct.bill().getPricePerUnit(b)),
                String.format("%.2f", acct.bill().getTotalAmount(b))
            });
        }
        exportTable("Bill Report / รายงานบิลการขายผลผลิต", "bill_report.txt",
            new int[] {8, 18, 14, 28, 12, 12, 16},
            new String[] {"Bill ID", "Bill No.", "Sale Date", "Buyer", "Qty (kg)", "Price/kg", "Total (THB)"},
            rows, "ไม่มีข้อมูลบิลสำหรับ export");
    }

    // ───────────────── helpers ─────────────────

    /** รายงานแบบตารางเดียว: header → หัวคอลัมน์ → แถว → footer → บันทึกไฟล์ */
    private void exportTable(String title, String fileName, int[] widths,
                             String[] headers, List<String[]> rows, String emptyMsg) {
        if (rows.isEmpty()) { showError(emptyOr(emptyMsg)); return; }

        StringBuilder sb = ReportExporter.header(title);
        sb.append(ReportExporter.separator(widths)).append("\n");
        sb.append(ReportExporter.row(headers, widths)).append("\n");
        sb.append(ReportExporter.separator(widths)).append("\n");
        for (String[] row : rows) sb.append(ReportExporter.row(row, widths)).append("\n");
        sb.append(ReportExporter.separator(widths)).append("\n");
        sb.append(ReportExporter.footer(rows.size()));
        save(fileName, sb.toString());
    }

    private void save(String fileName, String content) {
        boolean ok = ReportExporter.saveToFile(lblMsg.getScene().getWindow(), fileName, content);
        lblMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#aaaaaa;");
        lblMsg.setText(ok ? "Export สำเร็จ: " + fileName : "ยกเลิก");
    }

    /** ถ้าดึงข้อมูลไม่สำเร็จ (SQLException) ให้แสดงสาเหตุแทนข้อความ "ไม่มีข้อมูล" */
    private String emptyOr(String emptyMsg) {
        String err = !ctrl.getErrorMessage().isEmpty() ? ctrl.getErrorMessage() : acct.getErrorMessage();
        ctrl.clearMessages();
        acct.clearMessages();
        return err.isEmpty() ? emptyMsg : err;
    }

    private void showError(String msg) {
        lblMsg.setStyle("-fx-text-fill:#ff8888;");
        lblMsg.setText(msg);
    }
}
