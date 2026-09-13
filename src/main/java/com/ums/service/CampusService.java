package com.ums.service;

import com.ums.dto.CampusRequest;
import com.ums.dto.CampusResponse;

import java.util.List;

public interface CampusService {
    CampusResponse createCampus(CampusRequest request);
    List<CampusResponse> getAllCampuses();
    CampusResponse getCampusById(Long id);
}