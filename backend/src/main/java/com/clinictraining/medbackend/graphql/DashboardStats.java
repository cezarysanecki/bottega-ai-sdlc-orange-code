package com.clinictraining.medbackend.graphql;

public class DashboardStats {
    private int totalPatients;
    private int activeProviders;
    private int openInvoices;
    private int upcomingAppointments;

    public int getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(int totalPatients) {
        this.totalPatients = totalPatients;
    }

    public int getActiveProviders() {
        return activeProviders;
    }

    public void setActiveProviders(int activeProviders) {
        this.activeProviders = activeProviders;
    }

    public int getOpenInvoices() {
        return openInvoices;
    }

    public void setOpenInvoices(int openInvoices) {
        this.openInvoices = openInvoices;
    }

    public int getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(int upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }
}
