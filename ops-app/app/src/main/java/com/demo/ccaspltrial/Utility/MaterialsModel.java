package com.demo.ccaspltrial.Utility;

public class MaterialsModel {

    public String materialId="";
    public String materialCode="";
    public String materialName="";
    public String unit_quant="";
    public String materialRate="";
    public int materialQuantity=0;
    public int matRequireQuantity=0;
    public int materialChecked=0; //0 for unchecked, 1 for checked

    public MaterialsModel(){}

    public MaterialsModel(String name)
    {
        materialName=name;
    }

    public void setMaterialId(String materialId)
    {
        this.materialId=materialId;
    }

    public String getMaterialId()
    {
        return materialId;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialName(String name)
    {
        materialName=name;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setUnit_quant(String unit_quant)
    {
        this.unit_quant=unit_quant;
    }

    public String getUnit_quant()
    {
        return unit_quant;
    }

    public void setMaterialRate(String materialRate) {
        this.materialRate = materialRate;
    }

    public String getMaterialRate() {
        return materialRate;
    }

    public void setMaterialQuantity(int quantity)
    {
        materialQuantity=quantity;
    }

    public int getMaterialQuantity()
    {
        return materialQuantity;
    }

    public void setMatRequireQuantity(int reqQuantity)
    {
        matRequireQuantity=reqQuantity;
    }

    public int getMatRequireQuantity()
    {
        return matRequireQuantity;
    }

    public int getMaterialChecked() {
        return materialChecked;
    }

    public void setMaterialChecked(int materialChecked) {
        this.materialChecked = materialChecked;
    }
}
