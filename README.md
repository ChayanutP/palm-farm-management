## Database Setup (phpMyAdmin)

1. **Start Services:** เปิด **XAMPP Control Panel** และกด Start บริการ **Apache** และ **MySQL**
2. **Create Database:** เข้าไปที่ `http://localhost/phpmyadmin/` แล้วสร้างฐานข้อมูลใหม่ชื่อ `palm_farm` (ตั้งค่า Collation เป็น `utf8mb4_unicode_ci`)
3. **Import SQL File:**
   - คลิกเลือกฐานข้อมูล `palm_farm`
   - ไปที่แท็บ **Import** > กด **Choose File** แล้วเลือกไฟล์ `.sql` ในโฟลเดอร์ database ของโปรเจกต์
   - เลื่อนลงมาล่างสุดแล้วกด **Go** เพื่อนำเข้าข้อมูล
4. **Run Application:** เมื่อ Import เรียบร้อย สามารถสั่ง Run โปรแกรม JavaFX ได้ทันที