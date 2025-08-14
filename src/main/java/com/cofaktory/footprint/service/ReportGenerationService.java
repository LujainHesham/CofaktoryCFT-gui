package com.cofaktory.footprint.service;

import com.cofaktory.footprint.model.*;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;

import java.io.*;
import java.util.List;

public class ReportGenerationService {
    private final LanguageService languageService;

    public ReportGenerationService(LanguageService languageService) {
        this.languageService = languageService;
    }

    // Existing report for a single branch's emissions
    public void generateCarbonReportPDF(CarbonFootprintMetrics metrics, OutputStream outputStream) throws IOException {
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        TextAlignment alignment = languageService.isRTL() ? TextAlignment.RIGHT : TextAlignment.LEFT;

        Paragraph title = new Paragraph(languageService.getMessage("report.title"))
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        Paragraph sectionHeader = new Paragraph(languageService.getMessage("report.branch.info"))
                .setFontSize(16)
                .setBold()
                .setTextAlignment(alignment)
                .setMarginBottom(10);
        document.add(sectionHeader);

        float[] columnWidths = {150, 300};
        Table branchTable = new Table(columnWidths)
                .setTextAlignment(alignment)
                .setMarginBottom(20);

        branchTable.addCell(createCell(languageService.getMessage("report.branch.id"), true));
        branchTable.addCell(createCell(String.valueOf(metrics.getBranchId()), false));
        branchTable.addCell(createCell(languageService.getMessage("report.city"), true));
        branchTable.addCell(createCell(metrics.getCityName(), false));
        document.add(branchTable);

        Paragraph emissionsHeader = new Paragraph(languageService.getMessage("report.emissions"))
                .setFontSize(16)
                .setBold()
                .setTextAlignment(alignment)
                .setMarginBottom(10);
        document.add(emissionsHeader);

        Table emissionsTable = new Table(columnWidths)
                .setTextAlignment(alignment)
                .setMarginBottom(20);

        emissionsTable.addCell(createCell(languageService.getMessage("report.production.emissions"), true));
        emissionsTable.addCell(createCell(formatNumber(metrics.getProductionEmissions()) + " kg CO₂", false));
        emissionsTable.addCell(createCell(languageService.getMessage("report.packaging.emissions"), true));
        emissionsTable.addCell(createCell(formatNumber(metrics.getPackagingEmissions()) + " kg CO₂", false));
        emissionsTable.addCell(createCell(languageService.getMessage("report.distribution.emissions"), true));
        emissionsTable.addCell(createCell(formatNumber(metrics.getDistributionEmissions()) + " kg CO₂", false));
        emissionsTable.addCell(createCell(languageService.getMessage("report.total.emissions"), true));
        emissionsTable.addCell(createCell(formatNumber(metrics.getTotalEmissions()) + " kg CO₂", false).setBold());
        document.add(emissionsTable);

        Paragraph chartHeader = new Paragraph(languageService.getMessage("report.emissions.breakdown"))
                .setFontSize(16)
                .setBold()
                .setTextAlignment(alignment)
                .setMarginBottom(10);
        document.add(chartHeader);

        document.add(createBarChart(languageService.getMessage("report.production"), metrics.getProductionEmissions(), metrics.getTotalEmissions()));
        document.add(createBarChart(languageService.getMessage("report.packaging"), metrics.getPackagingEmissions(), metrics.getTotalEmissions()));
        document.add(createBarChart(languageService.getMessage("report.distribution"), metrics.getDistributionEmissions(), metrics.getTotalEmissions()));

        Paragraph footer = new Paragraph(languageService.getMessage("report.generated.on") + " " + new java.util.Date().toString())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);

        document.close();
    }

    // PDF for a city: list of branches/emissions
    public void generateCarbonReportPDF(List<CarbonFootprintMetrics> metricsList, OutputStream outputStream) throws IOException {
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        TextAlignment alignment = languageService.isRTL() ? TextAlignment.RIGHT : TextAlignment.LEFT;

        Paragraph title = new Paragraph(languageService.getMessage("report.city.title"))
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        float[] columnWidths = {80, 100, 100, 100, 100, 100};
        Table table = new Table(columnWidths).setTextAlignment(alignment).setMarginBottom(20);

        table.addHeaderCell(createCell(languageService.getMessage("report.branch.id"), true));
        table.addHeaderCell(createCell(languageService.getMessage("report.city"), true));
        table.addHeaderCell(createCell(languageService.getMessage("report.production.emissions"), true));
        table.addHeaderCell(createCell(languageService.getMessage("report.packaging.emissions"), true));
        table.addHeaderCell(createCell(languageService.getMessage("report.distribution.emissions"), true));
        table.addHeaderCell(createCell(languageService.getMessage("report.total.emissions"), true));

        double total = 0.0;
        for (CarbonFootprintMetrics m : metricsList) {
            table.addCell(createCell(String.valueOf(m.getBranchId()), false));
            table.addCell(createCell(m.getCityName(), false));
            table.addCell(createCell(formatNumber(m.getProductionEmissions()), false));
            table.addCell(createCell(formatNumber(m.getPackagingEmissions()), false));
            table.addCell(createCell(formatNumber(m.getDistributionEmissions()), false));
            table.addCell(createCell(formatNumber(m.getTotalEmissions()), false));
            total += m.getTotalEmissions();
        }

        document.add(table);

        Paragraph totalPara = new Paragraph(languageService.getMessage("report.total.emissions") + ": " + formatNumber(total) + " kg CO₂")
                .setFontSize(12)
                .setBold()
                .setTextAlignment(alignment)
                .setMarginBottom(10);
        document.add(totalPara);

        Paragraph footer = new Paragraph(languageService.getMessage("report.generated.on") + " " + new java.util.Date().toString())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);

        document.close();
    }

    // PDF for a single reduction plan
    public void generateReductionPlanReportPDF(ReductionStrategy plan, OutputStream outputStream) throws IOException {
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        TextAlignment alignment = languageService.isRTL() ? TextAlignment.RIGHT : TextAlignment.LEFT;

        Paragraph title = new Paragraph(languageService.getMessage("reduction.report.title"))
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        float[] columnWidths = {180, 300};
        Table table = new Table(columnWidths).setTextAlignment(alignment).setMarginBottom(20);

        table.addCell(createCell(languageService.getMessage("reduction.report.id"), true));
        table.addCell(createCell(String.valueOf(plan.getReductionId()), false));
        table.addCell(createCell(languageService.getMessage("reduction.report.branch.id"), true));
        table.addCell(createCell(String.valueOf(plan.getBranchId()), false));
        table.addCell(createCell(languageService.getMessage("reduction.report.user.id"), true));
        table.addCell(createCell(String.valueOf(plan.getUserId()), false));
        table.addCell(createCell(languageService.getMessage("reduction.report.strategy"), true));
        table.addCell(createCell(plan.getReductionStrategy(), false));
        table.addCell(createCell(languageService.getMessage("reduction.report.status"), true));
        table.addCell(createCell(String.valueOf(plan.getStatusId()), false));
        table.addCell(createCell(languageService.getMessage("reduction.report.implementation.costs"), true));
        table.addCell(createCell(formatNumber(plan.getImplementationCosts()), false));
        table.addCell(createCell(languageService.getMessage("reduction.report.projected.profits"), true));
        table.addCell(createCell(formatNumber(plan.getProjectedAnnualProfits()), false));

        document.add(table);

        Paragraph footer = new Paragraph(languageService.getMessage("report.generated.on") + " " + new java.util.Date().toString())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);

        document.close();
    }

    // Comparative report for list of branches (already present in your code)
    public void generateComparativeReportPDF(List<BranchMetrics> branches, OutputStream outputStream) throws IOException {
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        TextAlignment alignment = languageService.isRTL() ? TextAlignment.RIGHT : TextAlignment.LEFT;

        Paragraph title = new Paragraph(languageService.getMessage("report.comparative.title"))
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        double totalEmissions = branches.stream()
                .mapToDouble(BranchMetrics::getCarbonEmissionsKg)
                .sum();
        double avgEmissions = totalEmissions / branches.size();

        Paragraph summary = new Paragraph(
                String.format(languageService.getMessage("report.comparative.summary"),
                        branches.size(),
                        formatNumber(totalEmissions),
                        formatNumber(avgEmissions)))
                .setTextAlignment(alignment)
                .setMarginBottom(15);
        document.add(summary);

        float[] columnWidths = {100, 150, 100, 100, 100};
        Table compTable = new Table(columnWidths)
                .setTextAlignment(alignment)
                .setMarginBottom(20);

        compTable.addHeaderCell(createCell(languageService.getMessage("report.branch.id"), true));
        compTable.addHeaderCell(createCell(languageService.getMessage("report.location"), true));
        compTable.addHeaderCell(createCell(languageService.getMessage("report.employees"), true));
        compTable.addHeaderCell(createCell(languageService.getMessage("report.emissions"), true));
        compTable.addHeaderCell(createCell(languageService.getMessage("report.emissions.per.employee"), true));

        for (BranchMetrics branch : branches) {
            compTable.addCell(createCell(String.valueOf(branch.getBranchId()), false));
            compTable.addCell(createCell(branch.getLocation(), false));
            compTable.addCell(createCell(String.valueOf(branch.getNumberOfEmployees()), false));
            compTable.addCell(createCell(formatNumber(branch.getCarbonEmissionsKg()) + " kg", false));
            double perEmployee = branch.getNumberOfEmployees() > 0 ?
                    branch.getCarbonEmissionsKg() / branch.getNumberOfEmployees() : 0;
            compTable.addCell(createCell(formatNumber(perEmployee) + " kg", false));
        }

        document.add(compTable);

        Paragraph rankingHeader = new Paragraph(languageService.getMessage("report.top.performers"))
                .setFontSize(16)
                .setBold()
                .setTextAlignment(alignment)
                .setMarginBottom(10);
        document.add(rankingHeader);

        branches.sort((b1, b2) -> Double.compare(b1.getCarbonEmissionsKg(), b2.getCarbonEmissionsKg()));

        com.itextpdf.layout.element.List rankedList = new com.itextpdf.layout.element.List()
                .setSymbolIndent(20)
                .setTextAlignment(alignment);

        int count = Math.min(branches.size(), 5); // Top 5
        for (int i = 0; i < count; i++) {
            BranchMetrics branch = branches.get(i);
            rankedList.add(String.format("%s: %s - %s kg CO₂ (%s %s)",
                    languageService.getMessage("report.branch") + " " + branch.getBranchId(),
                    branch.getLocation(),
                    formatNumber(branch.getCarbonEmissionsKg()),
                    languageService.getMessage("report.employees"),
                    branch.getNumberOfEmployees()));
        }
        document.add(rankedList);

        Paragraph footer = new Paragraph(languageService.getMessage("report.generated.on") + " " + new java.util.Date().toString())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);

        document.close();
    }

    // --- Helpers ---
    private Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(content));
        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(220, 220, 220))
                    .setBold();
        }
        return cell;
    }

    private Paragraph createBarChart(String label, double value, double maxValue) {
        int width = 50;
        int barLength = maxValue > 0 ? (int) ((value / maxValue) * width) : 0;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) bar.append("█");
        return new Paragraph(String.format("%-20s: %s %s kg", label, bar.toString(), formatNumber(value)))
                .setFontSize(10)
                .setMarginBottom(5);
    }

    private String formatNumber(double value) {
        return String.format("%,.2f", value);
    }
}