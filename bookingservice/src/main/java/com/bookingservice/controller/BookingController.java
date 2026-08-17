package com.bookingservice.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookingservice.client.PaymentClient;
import com.bookingservice.client.PropertyClient;
import com.bookingservice.dto.APIResponse;
import com.bookingservice.dto.BookingDto;
import com.bookingservice.dto.ProductRequest;
import com.bookingservice.dto.PropertyDto;
import com.bookingservice.dto.RoomAvailability;
import com.bookingservice.dto.Rooms;
import com.bookingservice.dto.StripeResponse;
import com.bookingservice.entity.BookingDate;
import com.bookingservice.entity.Bookings;
import com.bookingservice.repository.BookingDateRepository;
import com.bookingservice.repository.BookingRepository;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

	@Autowired
	private PropertyClient propertyClient;

	@Autowired
	private PaymentClient paymentClient;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private BookingDateRepository bookingDateRepository;

	@PostMapping("/add-to-cart")
	public APIResponse<List<String>> cart(@RequestBody BookingDto bookingDto) {

		Optional<RoomAvailability> matchedRoom = java.util.Optional.empty();

		APIResponse<List<String>> apiResponse = new APIResponse<>();

		List<String> messages = new ArrayList<>();

		APIResponse<PropertyDto> response = propertyClient.getPropertyById(bookingDto.getPropertyId());

		APIResponse<Rooms> roomType = propertyClient.getRoomType(bookingDto.getRoomId());

		APIResponse<List<RoomAvailability>> totalRoomsAvailable = propertyClient.getTotalRoomsAvailable(bookingDto.getRoomAvailabilityId());

		List<RoomAvailability> availableRooms = totalRoomsAvailable.getData();

		//Logic to check available rooms based on date and count
		for(LocalDate date: bookingDto.getDate()) {
			boolean isAvailable = availableRooms.stream()
					.anyMatch(ra -> ra.getAvailableDate().equals(date) && ra.getAvailableCount()>0);

			System.out.println("Date " + date + " available: " + isAvailable);

			if (!isAvailable) {
				messages.add("Room not available on: " + date);
				apiResponse.setMessage("Sold Out");
				apiResponse.setStatus(500);
				apiResponse.setData(messages);
				return apiResponse;
			}

			matchedRoom = availableRooms.stream()
					.filter(ra -> ra.getAvailableDate().equals(date) && ra.getAvailableCount() > 0)
					.findFirst();
		}

		//Save it to Booking Table with status pending
		Bookings bookings = new Bookings();
		bookings.setName(bookingDto.getName());
		bookings.setEmail(bookingDto.getEmail());
		bookings.setMobile(bookingDto.getMobile());
		bookings.setPropertyName(response.getData().getName());
		bookings.setStatus("pending");
		bookings.setTotalPrice(roomType.getData().getBasePrice() * bookingDto.getTotalNigths());
		Bookings savedBooking = bookingRepository.save(bookings);

		for(LocalDate date: bookingDto.getDate()) {
			BookingDate bookingDate = new BookingDate();
			bookingDate.setDate(date);
			bookingDate.setBookings(savedBooking);
			bookingDateRepository.save(bookingDate);

			if(savedBooking != null && matchedRoom.isPresent()) {
				propertyClient.updateRoomCount(matchedRoom.get().getId(), date);
			}
		}

		messages.add("Booking added to cart successfully");
		apiResponse.setMessage("Success");
		apiResponse.setStatus(200);
		apiResponse.setData(messages);

		return apiResponse;
	}

	@PostMapping("/process-payment")
	public StripeResponse proceedPayment(@RequestBody ProductRequest productRequest ){
		StripeResponse stripeResponse = paymentClient.checkoutProducts(productRequest);
		return stripeResponse;
	}

	@PutMapping("/update-status-booking")
	public boolean updateBooking(@RequestParam long id) {
		Optional<Bookings> opBooking = bookingRepository.findById(id);
		if(opBooking.isPresent()) {
			Bookings bookings = opBooking.get();
			bookings.setStatus("Confirmed");
			Bookings savedBooking = bookingRepository.save(bookings);
			if(savedBooking!=null) {
				return true;
			}
		}
		return false;
	}
}