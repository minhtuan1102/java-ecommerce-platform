import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

const AddProduct = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [categoriesLoading, setCategoriesLoading] = useState(true);
  const [error, setError] = useState('');
  const [categories, setCategories] = useState([]);

  // State form cơ bản
  const [productData, setProductData] = useState({
    name: '',
    description: '',
    brand: '',
    categoryId: '',
    imageUrls: ['']
  });

  // State quản lý danh sách biến thể (SKUs)
  const [skus, setSkus] = useState([
    { skuCode: '', tierIndex: 'Mặc định', price: 0, stock: 0 }
  ]);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await api.get('/categories');
        setCategories(response.data);
      } catch {
        setError('Khong the tai danh muc. Vui long thu lai.');
      } finally {
        setCategoriesLoading(false);
      }
    };

    fetchCategories();
  }, []);

  const handleProductChange = (e) => {
    setProductData({ ...productData, [e.target.name]: e.target.value });
  };

  const handleImageUrlChange = (index, value) => {
    const newUrls = [...productData.imageUrls];
    newUrls[index] = value;
    setProductData({ ...productData, imageUrls: newUrls });
  };

  const addImageUrl = () => {
    setProductData({ ...productData, imageUrls: [...productData.imageUrls, ''] });
  };

  const handleSkuChange = (index, field, value) => {
    const newSkus = [...skus];
    newSkus[index][field] = value;
    setSkus(newSkus);
  };

  const addSku = () => {
    setSkus([...skus, { skuCode: '', tierIndex: '', price: 0, stock: 0 }]);
  };

  const removeSku = (index) => {
    if (skus.length > 1) {
      const newSkus = skus.filter((_, i) => i !== index);
      setSkus(newSkus);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    // Lọc bỏ ảnh rỗng
    const validImageUrls = productData.imageUrls.filter(url => url.trim() !== '');

    const payload = {
      ...productData,
      categoryId: parseInt(productData.categoryId),
      imageUrls: validImageUrls,
      skus: skus.map(sku => ({
        ...sku,
        price: parseFloat(sku.price),
        stock: parseInt(sku.stock)
      }))
    };

    try {
      await api.post('/products', payload);
      alert('Dang san pham thanh cong. San pham se cho Admin duyet truoc khi hien thi cong khai.');
      navigate('/my-shop');
    } catch (err) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra khi đăng sản phẩm. Vui lòng kiểm tra lại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto mt-8 p-6 bg-white rounded-lg shadow-sm border border-gray-200 mb-20">
      <h1 className="text-2xl font-bold mb-6 text-gray-800">Thêm Sản Phẩm Mới</h1>
      
      {error && <div className="bg-red-50 text-red-600 p-4 rounded mb-6 border border-red-100">{error}</div>}

      <form onSubmit={handleSubmit} className="space-y-8">
        {/* Thông tin cơ bản */}
        <div className="bg-gray-50 p-6 rounded-lg border border-gray-200">
          <h2 className="text-lg font-semibold mb-4 text-gray-700">1. Thông tin cơ bản</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tên sản phẩm *</label>
              <input type="text" name="name" required value={productData.name} onChange={handleProductChange}
                className="w-full p-2 border border-gray-300 rounded focus:ring-primary focus:border-primary outline-none" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Danh mục *</label>
                <select name="categoryId" required value={productData.categoryId} onChange={handleProductChange}
                  disabled={categoriesLoading || categories.length === 0}
                  className="w-full p-2 border border-gray-300 rounded focus:ring-primary outline-none bg-white">
                  <option value="">{categoriesLoading ? 'Dang tai danh muc...' : '-- Chon danh muc --'}</option>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Thương hiệu</label>
                <input type="text" name="brand" value={productData.brand} onChange={handleProductChange}
                  className="w-full p-2 border border-gray-300 rounded focus:ring-primary outline-none" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả sản phẩm</label>
              <textarea name="description" rows="4" value={productData.description} onChange={handleProductChange}
                className="w-full p-2 border border-gray-300 rounded focus:ring-primary outline-none"></textarea>
            </div>
          </div>
        </div>

        {/* Hình ảnh */}
        <div className="bg-gray-50 p-6 rounded-lg border border-gray-200">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-gray-700">2. Hình ảnh sản phẩm (URLs)</h2>
            <button type="button" onClick={addImageUrl} className="text-sm text-primary font-medium hover:underline">+ Thêm ảnh</button>
          </div>
          <div className="space-y-3">
            {productData.imageUrls.map((url, index) => (
              <div key={index} className="flex items-center gap-2">
                <span className="text-gray-500 text-sm w-16">Ảnh {index === 0 ? 'Bìa' : index + 1}</span>
                <input type="url" placeholder="https://example.com/image.jpg" value={url} onChange={(e) => handleImageUrlChange(index, e.target.value)}
                  className="flex-1 p-2 border border-gray-300 rounded focus:ring-primary outline-none" required={index === 0} />
              </div>
            ))}
          </div>
        </div>

        {/* Phân loại hàng (SKUs) */}
        <div className="bg-blue-50 p-6 rounded-lg border border-blue-100">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-blue-800">3. Phân loại hàng (Biến thể / SKU)</h2>
            <button type="button" onClick={addSku} className="text-sm bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700">+ Thêm phân loại</button>
          </div>
          
          <div className="overflow-x-auto">
            <table className="w-full text-left bg-white border border-gray-200 rounded">
              <thead className="bg-gray-100 text-gray-600 text-sm">
                <tr>
                  <th className="p-3 border-b">Tên Phân loại (Tier Index)</th>
                  <th className="p-3 border-b">Mã SKU</th>
                  <th className="p-3 border-b">Giá (VNĐ) *</th>
                  <th className="p-3 border-b">Kho hàng *</th>
                  <th className="p-3 border-b text-center">Xóa</th>
                </tr>
              </thead>
              <tbody>
                {skus.map((sku, index) => (
                  <tr key={index} className="border-b last:border-b-0 hover:bg-gray-50">
                    <td className="p-2">
                      <input type="text" placeholder="VD: Đỏ, Size M" value={sku.tierIndex} onChange={(e) => handleSkuChange(index, 'tierIndex', e.target.value)}
                        className="w-full p-2 border border-gray-300 rounded outline-none" required />
                    </td>
                    <td className="p-2">
                      <input type="text" placeholder="VD: SP-DO-M" value={sku.skuCode} onChange={(e) => handleSkuChange(index, 'skuCode', e.target.value)}
                        className="w-full p-2 border border-gray-300 rounded outline-none" required />
                    </td>
                    <td className="p-2">
                      <input type="number" min="0" value={sku.price} onChange={(e) => handleSkuChange(index, 'price', e.target.value)}
                        className="w-full p-2 border border-gray-300 rounded outline-none" required />
                    </td>
                    <td className="p-2">
                      <input type="number" min="0" value={sku.stock} onChange={(e) => handleSkuChange(index, 'stock', e.target.value)}
                        className="w-full p-2 border border-gray-300 rounded outline-none" required />
                    </td>
                    <td className="p-2 text-center">
                      <button type="button" onClick={() => removeSku(index)} disabled={skus.length === 1}
                        className={`text-red-500 font-bold ${skus.length === 1 ? 'opacity-30 cursor-not-allowed' : 'hover:text-red-700'}`}>
                        X
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="flex justify-end gap-4 pt-4 border-t">
          <button type="button" onClick={() => navigate('/my-shop')} className="px-6 py-2 border border-gray-300 rounded text-gray-700 hover:bg-gray-50 font-medium">Hủy</button>
          <button type="submit" disabled={loading} className={`bg-primary text-white px-8 py-2 rounded font-bold ${loading ? 'opacity-70' : 'hover:bg-red-600'}`}>
            {loading ? 'Đang lưu...' : 'Lưu & Đăng bán'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default AddProduct;
