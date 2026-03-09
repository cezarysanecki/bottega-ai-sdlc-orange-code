package com.clinictraining.medbackend.graphql;

import com.clinictraining.appointment.AppointmentService;
import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.billing.InvoiceService;
import com.clinictraining.billing.model.Invoice;
import com.clinictraining.billing.repository.InvoiceRepository;
import com.clinictraining.medbackend.api.ApiMapper;
import com.clinictraining.medbackend.api.dto.AppointmentRequest;
import com.clinictraining.medbackend.api.dto.AppointmentResponse;
import com.clinictraining.medbackend.api.dto.InvoiceResponse;
import com.clinictraining.medbackend.insurance.InsuranceEligibilityService;
import com.clinictraining.medbackend.insurance.InsuranceEligibilityView;
import com.clinictraining.medbackend.telehealth.GraphTelehealthSession;
import com.clinictraining.medbackend.telehealth.TelehealthService;
import com.clinictraining.medbackend.payments.PaymentRequest;
import com.clinictraining.medbackend.payments.PaymentService;
import com.clinictraining.patient.PatientDirectory;
import com.clinictraining.patient.PatientQueryService;
import com.clinictraining.patient.ProviderDirectory;
import com.clinictraining.patient.dto.PatientSummaryDto;
import com.clinictraining.patient.dto.ProviderSummaryDto;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class ClinicQueryResolver {

    private final AppointmentService appointmentService;
    private final PatientDirectory patientDirectory;
    private final PatientQueryService patientQueryService;
    private final ProviderDirectory providerDirectory;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final ApiMapper apiMapper;
    private final TelehealthService telehealthService;
    private final InsuranceEligibilityService insuranceEligibilityService;
    private final PaymentService paymentService;

    public ClinicQueryResolver(AppointmentService appointmentService,
                               PatientDirectory patientDirectory,
                               PatientQueryService patientQueryService,
                               ProviderDirectory providerDirectory,
                               InvoiceRepository invoiceRepository,
                               InvoiceService invoiceService,
                               ApiMapper apiMapper,
                               TelehealthService telehealthService,
                               InsuranceEligibilityService insuranceEligibilityService,
                               PaymentService paymentService) {
        this.appointmentService = appointmentService;
        this.patientDirectory = patientDirectory;
        this.patientQueryService = patientQueryService;
        this.providerDirectory = providerDirectory;
        this.invoiceRepository = invoiceRepository;
        this.invoiceService = invoiceService;
        this.apiMapper = apiMapper;
        this.telehealthService = telehealthService;
        this.insuranceEligibilityService = insuranceEligibilityService;
        this.paymentService = paymentService;
    }

    @QueryMapping
    public String clinicGreeting() {
        // Non-localized greeting is a known smell we plan to clean up later.
        return "Welcome to MedLink Clinics";
    }

    @QueryMapping
    public List<String> appointmentDigest() {
        return appointmentService.listUpcomingAppointments();
    }

    @QueryMapping
    public List<PatientSummaryDto> patients() {
        return patientQueryService.listPatients();
    }

    @QueryMapping
    public PatientSummaryDto patientByCode(@Argument String code) {
        return patientQueryService.findPatient(code);
    }

    @QueryMapping
    public List<ProviderSummaryDto> providers() {
        return providerDirectory.legacyTelehealthRoster();
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public List<InvoiceResponse> invoices(@Argument(name = "status") String status) {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> status == null || status.equalsIgnoreCase(invoice.getStatus()))
                .map(apiMapper::toResponse)
                .collect(Collectors.toList());
    }

    @QueryMapping
    public DashboardStats dashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalPatients(patientQueryService.listPatients().size());
        stats.setActiveProviders(providerDirectory.legacyTelehealthRoster().size());
        stats.setOpenInvoices((int) invoiceRepository.findAll().stream()
                .filter(invoice -> "OPEN".equalsIgnoreCase(invoice.getStatus()))
                .count());
        stats.setUpcomingAppointments(appointmentService.listUpcomingAppointments().size());
        return stats;
    }

    @QueryMapping
    public List<GraphTelehealthSession> telehealthSessions() {
        return telehealthService.refreshAll().stream()
                .map(GraphTelehealthSession::new)
                .toList();
    }

    @QueryMapping
    public GraphTelehealthSession telehealthSession(@Argument String vendorSessionId) {
        return new GraphTelehealthSession(telehealthService.syncSession(vendorSessionId));
    }

    @MutationMapping
    public AppointmentResponse createAppointment(@Argument AppointmentRequest request) {
        ZonedDateTime start = request.getStartTime();
        ZonedDateTime end = request.getEndTime();
        if (end != null && start != null && end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start");
        }
        Appointment appointment = appointmentService.bookAppointment(
                request.getPatientCode(),
                request.getProviderCode(),
                request.getAppointmentType(),
                request.getStartTime(),
                request.getEndTime(),
                request.getRoomCode()
        );
        return apiMapper.toResponse(appointment);
    }

    @MutationMapping
    public InvoiceResponse generateInvoice(@Argument String encounterCode) {
        Invoice invoice = invoiceService.generateInvoice(encounterCode);
        return apiMapper.toResponse(invoice);
    }

    @MutationMapping
    public GraphTelehealthSession syncTelehealthSession(@Argument String vendorSessionId) {
        return new GraphTelehealthSession(telehealthService.syncSession(vendorSessionId));
    }

    @MutationMapping
    public InsuranceEligibilityView checkInsuranceEligibility(@Argument String patientCode) {
        return insuranceEligibilityService.checkEligibility(patientCode);
    }

    @MutationMapping
    public Boolean invalidateInsuranceEligibility(@Argument String patientCode) {
        insuranceEligibilityService.invalidateCache(patientCode);
        return Boolean.TRUE;
    }

    @MutationMapping
    public InvoiceResponse postPayment(@Argument PaymentRequest input) {
        return apiMapper.toResponse(paymentService.recordPayment(input));
    }
}
