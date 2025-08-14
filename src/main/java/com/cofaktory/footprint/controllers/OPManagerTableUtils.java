package com.cofaktory.footprint.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class OPManagerTableUtils {

    public static void showAlert(String message) {
        Alert a = new Alert(AlertType.ERROR);
        a.setContentText(message);
        a.showAndWait();
    }

    public static void showInfo(String message) {
        Alert a = new Alert(AlertType.INFORMATION);
        a.setContentText(message);
        a.showAndWait();
    }

    public static void clearProductionFields(OPManagerDashboardController c) {
        if (c.supplierField != null) c.supplierField.clear();
        if (c.coffeeTypeComboBox != null) c.coffeeTypeComboBox.setValue(null);
        if (c.productTypeComboBox != null) c.productTypeComboBox.setValue(null);
        if (c.quantityField != null) c.quantityField.clear();
        if (c.productionDatePicker != null) c.productionDatePicker.setValue(null);
    }

    public static void clearPackagingFields(OPManagerDashboardController controller) {
        controller.packagingWasteField.clear();
        controller.packagingDatePicker.setValue(null);
    }

    public static void clearDistributionFields(OPManagerDashboardController controller) {
        controller.vehicleTypeComboBox.getSelectionModel().clearSelection();
        controller.numberOfVehiclesField.clear();
        controller.distancePerVehicleField.clear();
        controller.distributionDatePicker.setValue(null);
    }

    public static void clearBranchFields(OPManagerDashboardController c) {
        if (c.branchCityIdField != null) c.branchCityIdField.clear();
        if (c.branchLocationField != null) c.branchLocationField.clear();
        if (c.branchNumEmployeesField != null) c.branchNumEmployeesField.clear();
    }

    public static void clearUserFields(OPManagerDashboardController c) {
        if (c.userNameField != null) c.userNameField.clear();
        if (c.userRoleComboBox != null) c.userRoleComboBox.setValue(null);
        if (c.userEmailField != null) c.userEmailField.clear();
        if (c.userPasswordField != null) c.userPasswordField.clear();
    }
}