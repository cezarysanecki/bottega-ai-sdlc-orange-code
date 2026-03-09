package com.clinictraining.medbackend.api;

import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.billing.model.Encounter;
import com.clinictraining.billing.model.EncounterProcedure;
import com.clinictraining.medbackend.api.dto.AppointmentResponse;
import com.clinictraining.medbackend.api.dto.EncounterResponse;
import com.clinictraining.medbackend.api.dto.InvoiceItemResponse;
import com.clinictraining.medbackend.api.dto.InvoiceResponse;
import com.clinictraining.patient.dto.PatientSummaryDto;
import com.clinictraining.patient.dto.ProviderSummaryDto;
import com.clinictraining.patient.mapper.PatientMapper;
import com.clinictraining.patient.mapper.ProviderMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ApiMapper {

    private final PatientMapper patientMapper;
    private final ProviderMapper providerMapper;

    public ApiMapper(PatientMapper patientMapper, ProviderMapper providerMapper) {
        this.patientMapper = patientMapper;
        this.providerMapper = providerMapper;
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setAppointmentUid(appointment.getAppointmentUid());
        response.setAppointmentCode(appointment.getAppointmentCode());
        response.setStatus(appointment.getStatus().name());
        if (appointment.getSlot() != null) {
            response.setStartTime(appointment.getSlot().getStart());
            response.setEndTime(appointment.getSlot().getEnd());
        }
        response.setLegacyNotes(appointment.getLegacyNotes());
        response.setRoomCode(appointment.getRoom() != null ? appointment.getRoom().getRoomCode() : null);

        PatientSummaryDto patientSummary = patientMapper.toSummary(appointment.getPatient());
        ProviderSummaryDto providerSummary = providerMapper.toSummary(appointment.getProvider());
        response.setPatient(patientSummary);
        response.setProvider(providerSummary);
        return response;
    }

    public InvoiceResponse toResponse(com.clinictraining.billing.model.Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setStatus(invoice.getStatus());
        response.setIssuedAt(invoice.getIssuedAt());
        response.setDueDate(invoice.getDueDate());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setBalanceCents(invoice.getBalanceCents());
        response.setNotes(invoice.getNotes());

        invoice.getItems().forEach(item -> {
            InvoiceItemResponse itemResponse = new InvoiceItemResponse();
            itemResponse.setLineItemCode(item.getLineItemCode());
            itemResponse.setDescription(item.getDescription());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setUnitAmount(item.getUnitAmount());
            itemResponse.setLegacyTaxCents(item.getLegacyTaxCents());
            response.getItems().add(itemResponse);
        });
        return response;
    }

    public EncounterResponse toResponse(Encounter encounter) {
        EncounterResponse response = new EncounterResponse();
        response.setId(encounter.getId());
        response.setEncounterCode(encounter.getEncounterCode());
        response.setAppointmentId(encounter.getAppointment().getId());
        response.setAppointmentCode(encounter.getAppointment().getAppointmentCode());
        response.setEncounterTime(encounter.getEncounterTime());
        response.setReason(encounter.getReason());
        response.setSummary(encounter.getSummary());

        // Add patient and provider information
        PatientSummaryDto patientSummary = patientMapper.toSummary(encounter.getAppointment().getPatient());
        ProviderSummaryDto providerSummary = providerMapper.toSummary(encounter.getAppointment().getProvider());
        response.setPatient(patientSummary);
        response.setProvider(providerSummary);

        response.setProcedures(
                encounter.getProcedures().stream()
                        .map(this::toEncounterProcedureResponse)
                        .collect(Collectors.toList())
        );

        return response;
    }

    private EncounterResponse.EncounterProcedureResponse toEncounterProcedureResponse(EncounterProcedure ep) {
        EncounterResponse.EncounterProcedureResponse response = new EncounterResponse.EncounterProcedureResponse();
        response.setProcedureCode(ep.getProcedure().getProcedureCode());
        response.setDescription(ep.getProcedure().getDescription());
        response.setQuantity(ep.getQuantity());
        return response;
    }
}
