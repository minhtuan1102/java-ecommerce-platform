package com.tuan.ecommerce.modules.shipment.application;

import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.shipment.domain.Shipment;
import com.tuan.ecommerce.modules.shipment.domain.ShipmentStatus;
import com.tuan.ecommerce.modules.shipment.domain.ShipmentTrackingEvent;
import com.tuan.ecommerce.modules.shipment.infrastructure.persistence.ShipmentRepository;
import com.tuan.ecommerce.modules.shipment.infrastructure.persistence.ShipmentTrackingEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingEventRepository trackingEventRepository;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           ShipmentTrackingEventRepository trackingEventRepository) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
    }

    @Transactional
    public Shipment createShipment(Order order, String carrier, String trackingNumber) {
        Shipment shipment = Shipment.builder()
                .order(order)
                .status(ShipmentStatus.CREATED)
                .carrier(carrier)
                .trackingNumber(trackingNumber)
                .shippingAddress(order.getShippingAddress())
                .phoneNumber(order.getPhoneNumber())
                .build();

        Shipment saved = shipmentRepository.save(shipment);
        trackingEventRepository.save(buildTrackingEvent(saved, ShipmentStatus.CREATED, "Shipment created"));
        return saved;
    }

    @Transactional
    public Shipment updateShipmentStatus(Long shipmentId, ShipmentStatus status, String description) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));

        shipment.setStatus(status);
        if (status == ShipmentStatus.PICKED_UP || status == ShipmentStatus.IN_TRANSIT) {
            shipment.setShippedAt(LocalDateTime.now());
        }
        if (status == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        Shipment saved = shipmentRepository.save(shipment);
        trackingEventRepository.save(buildTrackingEvent(saved, status, description));
        return saved;
    }

    @Transactional(readOnly = true)
    public Shipment getShipmentByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));
    }

    @Transactional(readOnly = true)
    public List<ShipmentTrackingEvent> getTrackingEvents(Long shipmentId) {
        return trackingEventRepository.findByShipmentId(shipmentId);
    }

    private ShipmentTrackingEvent buildTrackingEvent(Shipment shipment, ShipmentStatus status, String description) {
        return ShipmentTrackingEvent.builder()
                .shipment(shipment)
                .status(status.name())
                .description(description)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}

