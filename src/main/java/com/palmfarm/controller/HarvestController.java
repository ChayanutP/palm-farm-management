package com.palmfarm.controller;

import com.palmfarm.controller.model.*;
import com.palmfarm.model.*;
import com.palmfarm.util.DBConnection;
import com.palmfarm.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class HarvestController extends PalmCareController {

    private final TaskModelController            taskMC = new TaskModelController();
    private final ReservationLineModelController rlMC   = new ReservationLineModelController();
    private final EmployeeModelController        empMC  = new EmployeeModelController();

}