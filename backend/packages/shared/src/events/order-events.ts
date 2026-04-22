export const ORDER_CREATED_EVENT = "order.created";
export const ORDER_STATUS_CHANGED_EVENT = "order.status.changed";

export interface BaseEvent<TType extends string, TPayload> {
  type: TType;
  payload: TPayload;
  occurredAt: string;
  correlationId: string;
}

export type OrderCreatedEvent = BaseEvent<
  typeof ORDER_CREATED_EVENT,
  {
    orderId: number;
    userId: number | null;
    totalPrice: number;
    status: string;
  }
>;

export type OrderStatusChangedEvent = BaseEvent<
  typeof ORDER_STATUS_CHANGED_EVENT,
  {
    orderId: number;
    previousStatus: string;
    nextStatus: string;
  }
>;

export type DomainEvent = OrderCreatedEvent | OrderStatusChangedEvent;
