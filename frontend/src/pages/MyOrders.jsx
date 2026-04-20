import React, { useEffect, useState } from 'react';
import api from '../api/axios';

const MyOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await api.get('/orders/my-orders');
        setOrders(response.data);
      } catch (err) {
        console.error('Failed to fetch orders', err);
      } finally {
        setLoading(false);
      }
    };
    fetchOrders();
  }, []);

  if (loading) return <div className="p-8 text-center text-gray-500 italic">Đang tải lịch sử đơn hàng...</div>;

  return (
    <div className="max-w-4xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-8 text-gray-800 uppercase">Đơn mua của tôi</h1>
      
      {orders.length === 0 ? (
        <div className="bg-white p-12 text-center rounded-lg border border-dashed text-gray-400">
          Bạn chưa có đơn hàng nào.
        </div>
      ) : (
        <div className="space-y-6">
          {orders.map((order) => (
            <div key={order.id} className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
              <div className="bg-gray-50 px-6 py-3 border-b flex justify-between items-center">
                <span className="font-bold text-gray-700">{order.shopName}</span>
                <span className={`text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider ${
                  order.status === 'DELIVERED' ? 'bg-green-100 text-green-700' : 
                  order.status === 'CANCELLED' ? 'bg-red-100 text-red-700' : 'bg-orange-100 text-orange-700'
                }`}>
                  {order.status}
                </span>
              </div>
              
              <div className="p-6">
                {order.items.map((item) => (
                  <div key={item.id} className="flex gap-4 mb-4 last:mb-0">
                    <div className="w-16 h-16 bg-gray-100 rounded border"></div>
                    <div className="flex-grow">
                      <h4 className="text-sm font-medium text-gray-800">{item.productName}</h4>
                      <p className="text-xs text-gray-400">Phân loại: {item.tierIndex} x {item.quantity}</p>
                    </div>
                    <div className="text-sm font-bold text-primary">₫{item.price?.toLocaleString()}</div>
                  </div>
                ))}
                
                <div className="mt-4 pt-4 border-t flex justify-between items-center">
                  <div className="text-xs text-gray-400">Mã đơn: #{order.id}</div>
                  <div>
                    <span className="text-gray-500 mr-2">Thành tiền:</span>
                    <span className="text-xl font-bold text-primary">₫{order.totalAmount?.toLocaleString()}</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyOrders;
