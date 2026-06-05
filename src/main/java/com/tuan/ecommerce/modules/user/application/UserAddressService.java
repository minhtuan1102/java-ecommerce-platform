package com.tuan.ecommerce.modules.user.application;

import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.user.application.dto.AddressResponse;
import com.tuan.ecommerce.modules.user.application.dto.CreateAddressRequest;
import com.tuan.ecommerce.modules.user.application.dto.UpdateAddressRequest;
import com.tuan.ecommerce.modules.user.domain.UserAddress;
import com.tuan.ecommerce.modules.user.infrastructure.mapper.UserAddressMapper;
import com.tuan.ecommerce.modules.user.infrastructure.persistence.UserAddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserAddressService {

    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;
    private final UserAddressMapper mapper;

    public UserAddressService(UserAddressRepository addressRepository,
                              UserRepository userRepository,
                              UserAddressMapper mapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(String userEmail) {
        User user = findUser(userEmail);
        return mapper.toResponseList(addressRepository.findByUserId(user.getId()));
    }

    @Transactional
    public AddressResponse createAddress(CreateAddressRequest request, String userEmail) {
        User user = findUser(userEmail);

        if (request.isDefaultAddress()) {
            addressRepository.clearDefaultForUser(user.getId());
        }

        UserAddress address = mapper.toEntity(request);
        address.setUser(user);
        UserAddress saved = addressRepository.save(address);
        return mapper.toResponse(saved);
    }

    @Transactional
    public AddressResponse updateAddress(Long addressId, UpdateAddressRequest request, String userEmail) {
        User user = findUser(userEmail);
        UserAddress address = findAddressForUser(addressId, user.getId());

        boolean setDefault = request.getDefaultAddress() != null && request.getDefaultAddress();
        if (setDefault) {
            addressRepository.clearDefaultForUser(user.getId());
        }

        mapper.updateEntity(address, request);
        UserAddress saved = addressRepository.save(address);
        return mapper.toResponse(saved);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long addressId, String userEmail) {
        User user = findUser(userEmail);
        addressRepository.clearDefaultForUser(user.getId());

        UserAddress address = findAddressForUser(addressId, user.getId());
        address.setDefaultAddress(true);
        return mapper.toResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long addressId, String userEmail) {
        User user = findUser(userEmail);
        UserAddress address = findAddressForUser(addressId, user.getId());
        addressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public UserAddress getAddressEntity(Long addressId, Long userId) {
        return findAddressForUser(addressId, userId);
    }

    private User findUser(String userEmail) {
        return userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserAddress findAddressForUser(Long addressId, Long userId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to access this address");
        }
        return address;
    }
}

