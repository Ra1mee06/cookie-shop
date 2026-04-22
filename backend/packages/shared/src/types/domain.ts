export interface User {
  id: number;
  username: string;
  email: string;
  password: string;
  fullName: string;
  role: "ADMIN" | "USER";
  avatarUrl: string | null;
}

export interface Product {
  id: number;
  title: string;
  price: number;
  imageUrl: string | null;
  description: string | null;
  ingredients: string | null;
  calories: string | null;
  story: string | null;
}

export interface OrderItemInput {
  productId: number;
  quantity: number;
  price: number;
}

export interface CreateOrderInput {
  items: OrderItemInput[];
  totalPrice: number;
  finalTotal?: number;
  discount?: number;
  promoCode?: string;
  recipient?: string;
  address?: string;
  comment?: string;
  paymentMethod?: "CASH" | "CARD_ONLINE" | "CARD_ON_DELIVERY";
  tip?: number;
}
