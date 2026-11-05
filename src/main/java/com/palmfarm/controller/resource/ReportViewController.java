package com.palmfarm.controller.resource;

import com.palmfarm.controller.AccountingController;
import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.ResourceModel;
import com.palmfarm.util.ReportExporter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        var tasks = ctrl.getLatestTaskPerGroup();
        int done = 0, allocated = 0, ordered = 0, received = 0;
        for (var t : tasks) {
            if (ctrl.task().isComplete(t))             done++;
            if (ctrl.task().isAllocated(t))            allocated++;
            if (ctrl.task().isOrdered(t))              ordered++;
            if (ctrl.task().getRequisitionStatus(t))   received++;
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
        exportTable("Latest Work Order Report / รายงานใบสั่งงานล่าสุด", "latest_work_order_report.txt",
            new int[] {8, 32, 22, 18, 16, 18, 14},
            new String[] {"Task ID", "Description", "Plantation", "Department", "Status", "Assigned", "Deadline"},
            rows, "ไม่มีข้อมูลใบสั่งงาน",
            List.of("ทั้งหมด: " + tasks.size() + " งาน",
                    "เสร็จสิ้น: " + done + " งาน | กำลังดำเนินการ: " + (tasks.size() - done) + " งาน",
                    "จัดสรรทรัพยากรแล้ว: " + allocated + " งาน",
                    "มอบหมายแล้ว: " + ordered + " งาน",
                    "มีผู้รับคำสั่งแล้ว: " + received + " งาน"));
    }

    /**
     * รายงานทรัพยากรที่ถูกใช้งานอยู่ในปัจจุบัน — มุมมอง resource เป็นหลัก
     * แสดงว่าทรัพยากรแต่ละชิ้นอยู่ที่งานไหน สถานะอะไร deadline เมื่อไหร่
     * กรองเฉพาะงานที่ยังไม่เสร็จ (is_complete = FALSE)
     */
    /**
     * รายงานทรัพยากรทั้งหมดพร้อมสถานะปัจจุบัน — มุมมอง resource เป็นหลัก
     * แสดงทุกชิ้น: ที่ว่าง (available) และที่ถูกจัดสรรอยู่ (reserved/in_use)
     * ช่วยให้ผู้จัดสรรเห็นภาพรวมว่าอะไรว่าง อะไรถูกใช้อยู่กับงานไหน
     */
    @FXML public void handleExportAllocation(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        var allocations = ctrl.getActiveResourceAllocations();
        int vehicles = 0, equipments = 0, available = 0, inUse = 0;
        for (var rl : allocations) {
            String type       = ctrl.rl().getResourceType(rl);
            String status     = ctrl.rl().getResourceStatus(rl);
            boolean hasTask   = ctrl.rl().getTaskId(rl) != 0;
            rows.add(new String[] {
                ctrl.rl().getResourceName(rl),
                type,
                status,
                hasTask ? String.valueOf(ctrl.rl().getTaskId(rl))  : "-",
                hasTask ? ctrl.rl().getTaskDescription(rl)         : "-",
                hasTask ? ctrl.rl().getPlantationName(rl)          : "-",
                hasTask ? ctrl.rl().getTaskDeadline(rl)            : "-"
            });
            if ("VEHICLE".equals(type))    vehicles++;
            else if ("EQUIPMENT".equals(type)) equipments++;
            if ("available".equals(status))    available++;
            else                               inUse++;
        }
        exportTable("Resource Allocation Report / รายงานสถานะทรัพยากรปัจจุบัน",
            "resource_allocation_report.txt",
            new int[] {28, 10, 12, 8, 26, 18, 14},
            new String[] {"Resource Name", "Type", "Status", "Task ID", "Task Description", "Plantation", "Deadline"},
            rows, "ไม่มีข้อมูลทรัพยากร",
            List.of("ทรัพยากรทั้งหมด: " + allocations.size() + " รายการ (VEHICLE: " + vehicles + " | EQUIPMENT: " + equipments + ")",
                    "ว่าง: " + available + " รายการ | ถูกใช้งาน: " + inUse + " รายการ"));
    }

    /**
     * รายงานผู้รับผิดชอบงานและทรัพยากรที่จัดสรร — แยกเป็นบล็อกต่อใบสั่งงาน พร้อมสรุปท้ายรายงาน
     * ผู้ออกคำสั่งคือพนักงานที่กดสร้างใบสั่งงาน (created_by) เป็นข้อมูลแสดงผลอย่างเดียว ไม่ผูกกับสิทธิ์ใด
     * ผู้รับผิดชอบคือพนักงานที่กดรับคำสั่ง (assigned_to) ถ้ายังไม่มีใครรับจะแสดง "ยังไม่มีผู้รับคำสั่ง"
     */
    @FXML public void handleExportResponsible(ActionEvent e) {
        int[] widths = {46, 12, 17};
        String[] headers = {"Resource Name", "Type", "Allocation Date"};
        StringBuilder sb = ReportExporter.header("Task Responsibility Report / รายงานผู้รับผิดชอบงานและทรัพยากรที่จัดสรร");
        int taskCount = 0, resourceCount = 0, vehicleCount = 0, equipmentCount = 0;
        for (var t : ctrl.getAllTasks()) {
            if (!ctrl.task().isAllocated(t)) continue;
            var rls = ctrl.getReservationsByTask(ctrl.task().getTaskId(t));
            if (rls.isEmpty()) continue;
            taskCount++;
            sb.append("\n[Task #").append(ctrl.task().getTaskId(t)).append("] ")
              .append(ctrl.task().getDescription(t))
              .append(" | ").append(ctrl.task().getPlantationName(t)).append("\n")
              .append("ผู้ออกคำสั่ง: ").append(ctrl.task().getCreatorDisplay(t))
              .append(" | ผู้รับผิดชอบ: ").append(ctrl.task().getResponsibleDisplay(t)).append("\n")
              .append(" | สถานะงาน: ").append(ctrl.task().getCompleteStatusDisplay(t)).append("\n");
            sb.append(ReportExporter.separator(widths)).append("\n");
            sb.append(ReportExporter.row(headers, widths)).append("\n");
            sb.append(ReportExporter.separator(widths)).append("\n");
            for (var rl : rls) {
                String type = ctrl.rl().getResourceType(rl);
                sb.append(ReportExporter.row(new String[] {
                    ctrl.rl().getResourceName(rl), type, ctrl.rl().getDate(rl)
                }, widths)).append("\n");
                resourceCount++;
                if ("VEHICLE".equals(type)) vehicleCount++;
                else if ("EQUIPMENT".equals(type)) equipmentCount++;
            }
            sb.append(ReportExporter.separator(widths)).append("\n");
        }
        if (taskCount == 0) { showError(emptyOr("ไม่มีข้อมูลการจัดสรรทรัพยากร")); return; }
        sb.append("\n").append(ReportExporter.summary(
            "จำนวนใบสั่งงานที่มีการจัดสรรทรัพยากรแล้ว: " + taskCount + " งาน",
            "รวมทรัพยากรที่ถูกจัดสรรทั้งหมด: " + resourceCount + " รายการ (VEHICLE: "
                + vehicleCount + " คัน, EQUIPMENT: " + equipmentCount + " ชิ้น)"));
        save("task_responsibility_report.txt", sb.toString());
    }

    // ───────────────── ทรัพยากร ─────────────────

    @FXML public void handleExportVehicle(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        var vehicles = ctrl.getResourcesByType("VEHICLE");
        for (var r : vehicles) {
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
            rows, "ไม่มีข้อมูลรถ", resourceSummary("รถ", "คัน", vehicles));
    }

    @FXML public void handleExportEquip(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        var equipments = ctrl.getResourcesByType("EQUIPMENT");
        for (var r : equipments) {
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
            rows, "ไม่มีข้อมูลอุปกรณ์", resourceSummary("อุปกรณ์", "ชิ้น", equipments));
    }

    // ───────────────── ข้อมูลหลัก ─────────────────

    @FXML public void handleExportPlantation(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        var plantations = ctrl.getAllPlantations();
        double totalArea = 0;
        for (var p : plantations) {
            totalArea += ctrl.plantation().getAreaRai(p);
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
            rows, "ไม่มีข้อมูลสวนปาล์ม",
            List.of("จำนวนแปลงทั้งหมด: " + plantations.size() + " แปลง",
                    String.format(Locale.US, "พื้นที่รวม: %,.2f ไร่", totalArea)));
    }

    @FXML public void handleExportEmployee(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        var employees = ctrl.getAllEmployees();
        Map<String, Integer> byDept = new LinkedHashMap<>();   // ตามลำดับฝ่ายที่ query เรียงมา
        for (var emp : employees) {
            byDept.merge(ctrl.task().getDepartmentDisplay(ctrl.employee().getDepartment(emp)), 1, Integer::sum);
            rows.add(new String[] {
                ctrl.employee().getNationalId(emp),
                ctrl.employee().getFullName(emp),
                ctrl.employee().getDepartment(emp),
                ctrl.employee().getPosition(emp),
                ctrl.employee().getPhone(emp)
            });
        }
        List<String> summary = new ArrayList<>();
        summary.add("พนักงานทั้งหมด: " + employees.size() + " คน");
        List<String> parts = new ArrayList<>();
        byDept.forEach((dept, n) -> parts.add(dept + ": " + n + " คน"));
        for (int i = 0; i < parts.size(); i += 3)                // บรรทัดละไม่เกิน 3 ฝ่าย
            summary.add(String.join(" | ", parts.subList(i, Math.min(i + 3, parts.size()))));
        exportTable("Employee Report / รายงานพนักงานทั้งหมด", "employee_report.txt",
            new int[] {15, 26, 18, 18, 14},
            new String[] {"บัตรประชาชน", "ชื่อ-สกุล", "ฝ่าย", "ตำแหน่ง", "โทรศัพท์"},
            rows, "ไม่มีข้อมูลพนักงาน", summary);
    }

    // ───────────────── บัญชี ─────────────────

    @FXML public void handleExportBill(ActionEvent e) {
        List<String[]> rows = new ArrayList<>();
        var bills = acct.getRecordedBills();
        double totalQty = 0, totalAmount = 0;
        for (var b : bills) {
            totalQty    += acct.bill().getPalmQuantity(b);
            totalAmount += acct.bill().getTotalAmount(b);
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
            rows, "ไม่มีข้อมูลบิลสำหรับ export",
            List.of("จำนวนบิลทั้งหมด: " + bills.size() + " ใบ",
                    String.format(Locale.US, "ปริมาณปาล์มรวม: %,.0f กก.", totalQty),
                    String.format(Locale.US, "มูลค่ารวม: %,.2f บาท", totalAmount)));
    }

    // ───────────────── helpers ─────────────────

    /** รายงานแบบตารางเดียว: header → หัวคอลัมน์ → แถว → footer → บันทึกไฟล์ */
    private void exportTable(String title, String fileName, int[] widths,
                             String[] headers, List<String[]> rows, String emptyMsg) {
        exportTable(title, fileName, widths, headers, rows, emptyMsg, null);
    }

    /** ถ้าส่ง summary มา ท้ายรายงานจะแสดงส่วนสรุปแทน Total Records */
    private void exportTable(String title, String fileName, int[] widths,
                             String[] headers, List<String[]> rows, String emptyMsg,
                             List<String> summary) {
        if (rows.isEmpty()) { showError(emptyOr(emptyMsg)); return; }

        StringBuilder sb = ReportExporter.header(title);
        sb.append(ReportExporter.separator(widths)).append("\n");
        sb.append(ReportExporter.row(headers, widths)).append("\n");
        sb.append(ReportExporter.separator(widths)).append("\n");
        for (String[] row : rows) sb.append(ReportExporter.row(row, widths)).append("\n");
        sb.append(ReportExporter.separator(widths)).append("\n");
        sb.append(summary == null
            ? ReportExporter.footer(rows.size())
            : ReportExporter.summary(summary.toArray(new String[0])));
        save(fileName, sb.toString());
    }

    /**
     * สรุปจำนวนทรัพยากรตามสถานะ (ใช้ร่วมกันระหว่างรายงานรถและอุปกรณ์)
     * available = ว่าง | reserved = จัดสรรให้งานแล้วแต่ยังไม่เบิก | in_use = เบิกไปใช้งานแล้ว
     */
    private List<String> resourceSummary(String label, String unit, List<ResourceModel> list) {
        int available = 0, reserved = 0, inUse = 0;
        for (var r : list) {
            String status = ctrl.resource().getStatus(r);
            if ("available".equals(status))     available++;
            else if ("reserved".equals(status)) reserved++;
            else if ("in_use".equals(status))   inUse++;
        }
        return List.of(
            label + "ทั้งหมด: " + list.size() + " " + unit,
            "ว่าง: " + available + " " + unit,
            "ถูกจอง (จัดสรรแล้ว ยังไม่เบิก): " + reserved + " " + unit,
            "กำลังใช้งาน (เบิกแล้ว): " + inUse + " " + unit);
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