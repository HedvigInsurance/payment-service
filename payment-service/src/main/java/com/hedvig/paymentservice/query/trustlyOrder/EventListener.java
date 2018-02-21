package com.hedvig.paymentservice.query.trustlyOrder;


import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderType;
import com.hedvig.paymentservice.domain.trustlyOrder.events.*;
import com.hedvig.paymentservice.query.trustlyOrder.enteties.TrustlyOrder;
import com.hedvig.paymentservice.query.trustlyOrder.enteties.TrustlyOrderRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    private final TrustlyOrderRepository orderRepository;

    public EventListener(TrustlyOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent e) {
        TrustlyOrder order = new TrustlyOrder();

        order.setId(e.getHedvigOrderId());

        orderRepository.save(order);
    }

    @EventHandler
    public void on(OrderAssignedTrustlyIdEvent e) {
        TrustlyOrder order = orderRepository.findOne(e.getHedvigOrderId());
        order.setState(OrderState.CONFIRMED);
        order.setTrustlyOrderId(e.getTrustlyOrderId());
        orderRepository.save(order);
    }

    @EventHandler
    public void on(OrderCanceledEvent e) {
        TrustlyOrder order = orderRepository.findOne(e.getHedvigOrderId());
        order.setState(OrderState.CANCELED);
        orderRepository.save(order);
    }

    @EventHandler
    public void on(OrderCompletedEvent e) {
        TrustlyOrder order = orderRepository.findOne(e.getId());
        order.setState(OrderState.COMPLETE);
        orderRepository.save(order);
    }

    @EventHandler
    public void on(SelectAccountResponseReceivedEvent e) {
        TrustlyOrder order = orderRepository.findOne(e.getHedvigOrderId());
        order.setIframeUrl(e.getIframeUrl());
        order.setType(OrderType.SELECT_ACCOUNT);
        orderRepository.save(order);
    }


}
