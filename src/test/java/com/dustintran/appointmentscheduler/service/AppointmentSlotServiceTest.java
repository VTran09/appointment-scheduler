package com.dustintran.appointmentscheduler.service;

import com.dustintran.appointmentscheduler.model.*;
import com.dustintran.appointmentscheduler.repository.AppointmentSlotRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentSlotServiceTest {

    @Mock
    AppointmentSlotRepository slotRepository;

    @InjectMocks
    AppointmentSlotService appointmentSlotService;

    private CourseSection section;
    private StudentGroup group;
    private AppointmentSlot baseSlot;

    @BeforeEach
    void setUp() {
        section = CourseSection.builder()
                .id(1L)
                .name("Project I")
                .build();

        group = StudentGroup.builder()
                .id(2L)
                .name("Group 1")
                .section(section)
                .build();

        baseSlot = AppointmentSlot.builder()
                .title("Sprint Interview")
                .type(AppointmentSlot.Type.INDIVIDUAL)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(15))
                .location("Luddy 2022")
                .capacity(1)
                .section(section)
                .targetScope(AppointmentSlot.TargetScope.ENTIRE_SECTION)
                .build();
    }

    @Test
    void create_success_individual_forcesCapacityOneAndActive() {
        when(slotRepository.save(any(AppointmentSlot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentSlot saved = appointmentSlotService.create(baseSlot);

        assertEquals(1, saved.getCapacity());
        assertEquals(AppointmentSlot.Status.ACTIVE, saved.getStatus());
    }

    @Test
    void create_throws_whenTitleBlank() {
        baseSlot.setTitle(" ");

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentSlotService.create(baseSlot));

        assertEquals("Title is required", ex.getMessage());
    }

    @Test
    void create_throws_whenLocationBlank() {
        baseSlot.setLocation("");

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentSlotService.create(baseSlot));

        assertEquals("Location is required", ex.getMessage());
    }

    @Test
    void create_throws_whenTimesMissing() {
        baseSlot.setStartTime(null);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentSlotService.create(baseSlot));

        assertEquals("Start and end time required", ex.getMessage());
    }

    @Test
    void create_throws_whenEndNotAfterStart() {
        LocalDateTime t = LocalDateTime.now().plusDays(1);
        baseSlot.setStartTime(t);
        baseSlot.setEndTime(t);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentSlotService.create(baseSlot));

        assertEquals("End time must be after start time", ex.getMessage());
    }

    @Test
    void create_throws_whenStartNotInFuture() {
        baseSlot.setStartTime(LocalDateTime.now().minusMinutes(1));
        baseSlot.setEndTime(LocalDateTime.now().plusMinutes(10));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentSlotService.create(baseSlot));

        assertEquals("Start time must be in the future", ex.getMessage());
    }

    @Test
    void create_throws_whenSpecificGroupButGroupMissing() {
        baseSlot.setTargetScope(AppointmentSlot.TargetScope.SPECIFIC_GROUP);
        baseSlot.setGroup(null);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentSlotService.create(baseSlot));

        assertEquals("Group is requied for a group-targeted slot", ex.getMessage());
    }

    @Test
    void create_throws_whenGroupBelongsToDifferentSection() {
        CourseSection otherSection = CourseSection.builder().id(99L).name("Project II").build();
        StudentGroup otherGroup = StudentGroup.builder()
                .id(5L)
                .name("Group X")
                .section(otherSection)
                .build();

        baseSlot.setTargetScope(AppointmentSlot.TargetScope.SPECIFIC_GROUP);
        baseSlot.setGroup(otherGroup);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentSlotService.create(baseSlot));

        assertEquals("Selected group does not belong to the selected section", ex.getMessage());
    }

    @Test
    void create_groupDefaultsCapacityToTen_whenLessThanTwo() {
        baseSlot.setType(AppointmentSlot.Type.GROUP);
        baseSlot.setCapacity(1);
        baseSlot.setTargetScope(AppointmentSlot.TargetScope.SPECIFIC_GROUP);
        baseSlot.setGroup(group);

        when(slotRepository.save(any(AppointmentSlot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentSlot saved = appointmentSlotService.create(baseSlot);

        assertEquals(10, saved.getCapacity());
        assertEquals(AppointmentSlot.Status.ACTIVE, saved.getStatus());
    }

    @Test
    void create_entireSectionClearsGroup() {
        baseSlot.setGroup(group);
        baseSlot.setTargetScope(AppointmentSlot.TargetScope.ENTIRE_SECTION);

        when(slotRepository.save(any(AppointmentSlot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentSlot saved = appointmentSlotService.create(baseSlot);

        assertNull(saved.getGroup());
    }

    @Test
    void cancel_throws_whenSlotMissing() {
        when(slotRepository.findById(123L)).thenReturn(Optional.empty());

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentSlotService.cancel(123L));

        assertEquals("Slot not found", ex.getMessage());
    }

    @Test
    void cancel_setsStatusCancelled() {
        AppointmentSlot slot = AppointmentSlot.builder()
                .id(10L)
                .status(AppointmentSlot.Status.ACTIVE)
                .build();

        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));

        appointmentSlotService.cancel(10L);

        assertEquals(AppointmentSlot.Status.CANCELLED, slot.getStatus());
        verify(slotRepository).save(slot);
    }
}