package com.app.DTO.response;

import java.util.List;

public record BoardResponse(long id, String name, UserDTO userDTO, List<PinDTO> pinDTOs) {}
