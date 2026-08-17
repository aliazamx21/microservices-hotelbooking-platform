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

	// ✅ Return success response instead of null
	messages.add("Booking added to cart successfully");
	apiResponse.setMessage("Success");
	apiResponse.setStatus(200);
	apiResponse.setData(messages);

	return apiResponse;
}