import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import { useNavigate } from 'react-router-dom';

const Cart = () => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchCart = async () => {
    try {
      const response = await api.get('/cart');
      setCart(response.data);
    } catch (err) {
      console.error('Failed to fetch cart', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    (async () => {
      await fetchCart();
    })();
  }, []);

  const handleRemoveItem = async (itemId) => {
    try {
      await api.delete(`/cart/${itemId}`);
      fetchCart(); // Refresh data
    } catch {
      alert('Không thể xóa sản phẩm.');
    }
  };

  const handleCheckout = () => {
    alert('Tính năng thanh toán đang được xử lý ngầm (Pessimistic Locking). Đang tạo đơn hàng...');
    api.post('/orders/checkout')
      .then(() => {
        alert('Đặt hàng thành công!');
        navigate('/');
      })
      .catch(err => {
        alert(err.response?.data?.message || 'Có lỗi xảy ra khi đặt hàng.');
      });
  };

  if (loading) return <div className="p-8 text-center">Đang tải giỏ hàng...</div>;

  if (!cart || !cart.shops || cart.shops.length === 0) {
    return (
      <div className="max-w-4xl mx-auto mt-10 p-12 bg-white rounded-lg shadow-sm text-center border border-gray-100">
        <div className="text-6xl mb-4">🛒</div>
        <p className="text-gray-500 mb-6">Giỏ hàng của bạn còn trống.</p>
        <button onClick={() => navigate('/')} className="bg-primary text-white px-8 py-2 rounded-md font-bold uppercase">Mua sắm ngay</button>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-8 text-gray-800 uppercase tracking-tight">Giỏ hàng</h1>
      
      <div className="space-y-6">
        {cart.shops.map((shop) => (
          <div key={shop.shopId} className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
            {/* Shop Header */}
            <div className="bg-gray-50 px-6 py-3 border-b flex items-center gap-3">
              <span className="font-bold text-gray-800">{shop.shopName}</span>
              <button className="text-primary text-xs hover:underline">Xem shop</button>
            </div>

            {/* Items */}
            <div className="divide-y divide-gray-50">
              {shop.items.map((item) => (
                <div key={item.id} className="p-6 flex items-center gap-6">
                  <div className="w-20 h-20 bg-gray-100 rounded border flex-shrink-0">
                    {/* Placeholder image */}
                  </div>
                  <div className="flex-grow">
                    <h4 className="text-gray-800 font-medium line-clamp-1">{item.productName}</h4>
                    <p className="text-gray-400 text-xs mt-1">Phân loại: {item.tierIndex}</p>
                    <div className="mt-2 text-primary font-bold">₫{item.price?.toLocaleString()}</div>
                  </div>
                  <div className="w-24 text-center">
                    <div className="text-gray-500 text-sm">Số lượng</div>
                    <div className="font-bold">{item.quantity}</div>
                  </div>
                  <div className="w-32 text-right">
                    <div className="text-gray-400 text-xs">Thành tiền</div>
                    <div className="text-primary font-bold font-mono">₫{item.subtotal?.toLocaleString()}</div>
                  </div>
                  <button onClick={() => handleRemoveItem(item.id)} className="text-gray-300 hover:text-red-500 transition px-2">
                    Xóa
                  </button>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Footer / Summary */}
      <div className="mt-8 bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col md:flex-row items-center justify-between gap-6">
        <div>
          <span className="text-gray-500 mr-2">Tổng thanh toán:</span>
          <span className="text-3xl font-bold text-primary font-mono">₫{cart.totalAmount?.toLocaleString()}</span>
        </div>
        <div className="flex gap-4 w-full md:w-auto">
          <button onClick={() => navigate('/')} className="flex-1 md:px-8 py-3 border border-gray-300 rounded-md text-gray-600 font-bold hover:bg-gray-50 transition">Tiếp tục mua sắm</button>
          <button onClick={handleCheckout} className="flex-1 md:px-12 py-3 bg-primary text-white rounded-md font-bold uppercase hover:bg-red-600 transition">Đặt hàng</button>
        </div>
      </div>
    </div>
  );
};

export default Cart;
