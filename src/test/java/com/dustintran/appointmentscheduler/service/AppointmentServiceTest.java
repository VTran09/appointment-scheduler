package com.dustintran.appointmentscheduler.service;

import com.dustintran.appointmentscheduler.model.*;
import com.dustintran.appointmentscheduler.repository.AppointmentRepository;
import com.dustintran.appointmentscheduler.repository.AppointmentSlotRepository;
import com.dustintran.appointmentscheduler.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    AppointmentRepository appointmentRepository;

    @Mock
    AppointmentSlotRepository slotRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AppointmentService appointmentService;

    private CourseSection section;
    private StudentGroup groupA;
    private StudentGroup groupB;
    private User student;
    private User teacher;
    private AppointmentSlot individualSlot;
    private AppointmentSlot groupSlot;

    @BeforeEach
    void setUp() {
        section = CourseSection.builder()
                .id(1L)
                .name("Project I")
                .build();

        groupA = StudentGroup.builder()
                .id(10L)
                .name("Group 1")
                .section(section)
                .build();

        groupB = StudentGroup.builder()
                .id(11L)
                .name("Group 2")
                .section(section)
                .build();

        student = User.builder()
                .id(100L)
                .username("student1")
                .password("pw")
                .role("STUDENT")
                .section(section)
                .group(groupA)
                .build();

        teacher = User.builder()
                .id(200L)
                .username("teacher1")
                .password("pw")
                .role("TEACHER")
                .section(section)
                .build();

        individualSlot = AppointmentSlot.builder()
                .id(1000L)
                .title("Sprint Interview")
                .type(AppointmentSlot.Type.INDIVIDUAL)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(15))
                .location("Luddy 2022")
                .capacity(1)
                .status(AppointmentSlot.Status.ACTIVE)
                .section(section)
                .targetScope(AppointmentSlot.TargetScope.ENTIRE_SECTION)
                .build();

        groupSlot = AppointmentSlot.builder()
                .id(2000L)
                .title("Group Check-in")
                .type(AppointmentSlot.Type.GROUP)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(20))
                .location("Luddy 2022")
                .capacity(5)
                .status(AppointmentSlot.Status.ACTIVE)
                .section(section)
                .targetScope(AppointmentSlot.TargetScope.SPECIFIC_GROUP)
                .group(groupA)
                .build();
    }

    @Test
    void bookSlot_success_individual() {
        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(appointmentRepository.existsBySlot_IdAndStudent_Username(1000L, "student1")).thenReturn(false);
        when(appointmentRepository.countBySlot_Id(1000L)).thenReturn(0L);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.bookSlot(1000L, "student1");

        assertNotNull(result);
        assertEquals(individualSlot, result.getSlot());
        assertEquals(student, result.getStudent());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void bookSlot_throws_whenSlotNotFound() {
        when(slotRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(999L, "student1"));

        assertEquals("Slot not found", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenSlotCancelled() {
        individualSlot.setStatus(AppointmentSlot.Status.CANCELLED);
        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(1000L, "student1"));

        assertEquals("This slot is not active", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenSlotInPast() {
        individualSlot.setStartTime(LocalDateTime.now().minusMinutes(5));
        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(1000L, "student1"));

        assertEquals("You can only book future slots", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenUserNotFound() {
        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(1000L, "missing"));

        assertEquals("Student not found", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenUserIsNotStudent() {
        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));
        when(userRepository.findByUsername("teacher1")).thenReturn(Optional.of(teacher));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(1000L, "teacher1"));

        assertEquals("Only students can book appointments", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenStudentHasNoSection() {
        student.setSection(null);
        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(1000L, "student1"));

        assertEquals("You are not assgined to a section", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenStudentInWrongSection() {
        CourseSection other = CourseSection.builder().id(2L).name("Project II").build();
        student.setSection(other);

        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(1000L, "student1"));

        assertEquals("You are not allowed to book this slot", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenIndividualAlreadyBooked() {
        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(appointmentRepository.existsBySlot_IdAndStudent_Username(1000L, "student1")).thenReturn(true);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(1000L, "student1"));

        assertEquals("You already booked this slot", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenIndividualSlotFull() {
        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(appointmentRepository.existsBySlot_IdAndStudent_Username(1000L, "student1")).thenReturn(false);
        when(appointmentRepository.countBySlot_Id(1000L)).thenReturn(1L);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(1000L, "student1"));

        assertEquals("This slot is already full", ex.getMessage());
    }

    @Test
    void bookSlot_success_group() {
        when(slotRepository.findById(2000L)).thenReturn(Optional.of(groupSlot));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(appointmentRepository.existsBySlot_IdAndStudent_Group(2000L, groupA)).thenReturn(false);
        when(appointmentRepository.countBySlot_Id(2000L)).thenReturn(0L);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.bookSlot(2000L, "student1");

        assertNotNull(result);
        assertEquals(groupSlot, result.getSlot());
        assertEquals(student, result.getStudent());
    }

    @Test
    void bookSlot_throws_whenGroupStudentHasNoGroup() {
        student.setGroup(null);
        when(slotRepository.findById(2000L)).thenReturn(Optional.of(groupSlot));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(2000L, "student1"));

        assertEquals("You are not assigned to a group", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenSpecificGroupSlotForAnotherGroup() {
        student.setGroup(groupB);
        when(slotRepository.findById(2000L)).thenReturn(Optional.of(groupSlot));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(2000L, "student1"));

        assertEquals("This slot is reserved for another group", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenGroupAlreadyBooked() {
        when(slotRepository.findById(2000L)).thenReturn(Optional.of(groupSlot));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(appointmentRepository.existsBySlot_IdAndStudent_Group(2000L, groupA)).thenReturn(true);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(2000L, "student1"));

        assertEquals("Your group already booked this slot", ex.getMessage());
    }

    @Test
    void countBookingsForSlots_returnsEmptyMap_whenNullOrEmpty() {
        assertTrue(appointmentService.countBookingsForSlots(null).isEmpty());
        assertTrue(appointmentService.countBookingsForSlots(java.util.List.of()).isEmpty());
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void bookSlot_throws_whenAnotherStudentTriesToBookFullIndividualSlot() {
        User student2 = User.builder()
                .id(101L)
                .username("student2")
                .password("pw")
                .role("STUDENT")
                .section(section)
                .group(groupA)
                .build();

        when(slotRepository.findById(1000L)).thenReturn(Optional.of(individualSlot));
        when(userRepository.findByUsername("student2")).thenReturn(Optional.of(student2));
        when(appointmentRepository.existsBySlot_IdAndStudent_Username(1000L, "student2")).thenReturn(false);
        when(appointmentRepository.countBySlot_Id(1000L)).thenReturn(1L);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(1000L, "student2"));

        assertEquals("This slot is already full", ex.getMessage());
    }

    @Test
    void bookSlot_throws_whenAnotherMemberOfSameGroupTriesToBookGroupSlot() {
        User student2 = User.builder()
                .id(102L)
                .username("student2")
                .password("pw")
                .role("STUDENT")
                .section(section)
                .group(groupA)
                .build();

        when(slotRepository.findById(2000L)).thenReturn(Optional.of(groupSlot));
        when(userRepository.findByUsername("student2")).thenReturn(Optional.of(student2));
        when(appointmentRepository.existsBySlot_IdAndStudent_Group(2000L, groupA)).thenReturn(true);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> appointmentService.bookSlot(2000L, "student2"));

        assertEquals("Your group already booked this slot", ex.getMessage());
    }
}