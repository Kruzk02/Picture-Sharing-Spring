package com.app.DTO.response;

import com.app.Model.Pin;

import java.util.List;

public record GetAllPinResponse(List<Pin> pins) { }
