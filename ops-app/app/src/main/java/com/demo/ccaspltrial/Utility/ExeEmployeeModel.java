package com.demo.ccaspltrial.Utility;

public class ExeEmployeeModel {

    public String empId="";
    public String empCode="";
    public String empName="";
    public String desigId="";
    public String desigName="";
    public String emp_type=""; //0 for regular employee, 1 for reliever, 2 for temporary reliever
    public String sub_regular_empId=""; //for relievers and temporary relievers, 0 for regular employees

    public ExeEmployeeModel(){}

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getDesigId() {
        return desigId;
    }

    public void setDesigId(String desigId) {
        this.desigId = desigId;
    }

    public String getDesigName() {
        return desigName;
    }

    public void setDesigName(String desigName) {
        this.desigName = desigName;
    }

    public String getEmp_type() {
        return emp_type;
    }

    public void setEmp_type(String emp_type) {
        this.emp_type = emp_type;
    }

    public String getSub_regular_empId() {
        return sub_regular_empId;
    }

    public void setSub_regular_empId(String sub_regular_empId) {
        this.sub_regular_empId = sub_regular_empId;
    }
}
