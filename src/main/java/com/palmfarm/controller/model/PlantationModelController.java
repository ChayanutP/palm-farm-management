package com.palmfarm.controller.model;

import com.palmfarm.model.PlantationModel;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlantationModelController {

    public int    getPlantationId(PlantationModel p) { return p.plantation_id; }
    public String getName(PlantationModel p)         { return p.name; }
    public double getAreaRai(PlantationModel p)      { return p.area_rai; }
    public String getLocation(PlantationModel p)     { return p.location; }

    public void setPlantationId(PlantationModel p, int v)    { p.plantation_id = v; }
    public void setName(PlantationModel p, String v)         { p.name = v; }
    public void setAreaRai(PlantationModel p, double v)      { p.area_rai = v; }
    public void setLocation(PlantationModel p, String v)     { p.location = v; }

    /** ชื่อย่อสำหรับ ComboBox: "ชื่อสวน (XX ไร่)" */
    public String getDisplayName(PlantationModel p) {
        return p.name + " (" + p.area_rai + " ไร่)";
    }

    public PlantationModel createEmpty() { return new PlantationModel(); }

    public PlantationModel mapFromResultSet(ResultSet rs) throws SQLException {
        PlantationModel p = new PlantationModel();
        p.plantation_id = rs.getInt("plantation_id");
        p.name          = rs.getString("name");
        p.area_rai      = rs.getDouble("area_rai");
        p.location      = rs.getString("location");
        return p;
    }
}
