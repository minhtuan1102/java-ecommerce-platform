import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api from '../api/axios';
import AdminLayout from './AdminLayout';
import Notice from '../components/Notice';
import { openSignedUploadWidget } from '../utils/cloudinaryUpload';
import { getApiError } from '../utils/format';

const emptyProduct = { name: '', description: '', categoryId: '', brandId: '', images: [] };
const emptySku = { skuCode: '', tierIndex: 'Mặc định', price: '', stock: '' };
const emptySpec = { key: '', value: '', displayOrder: 0 };

const ProductForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const editing = Boolean(id);
  const [product, setProduct] = useState(emptyProduct);
  const [skus, setSkus] = useState([emptySku]);
  const [specs, setSpecs] = useState([]);
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [quickCategory, setQuickCategory] = useState({ name: '', description: '' });
  const [quickBrand, setQuickBrand] = useState({ name: '', description: '' });
  const [loading, setLoading] = useState(editing);
  const [saving, setSaving] = useState(false);
  const [quickSaving, setQuickSaving] = useState({ category: false, brand: false });
  const [notice, setNotice] = useState({ type: '', text: '' });

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const [categoryRes, brandRes] = await Promise.all([api.get('/categories'), api.get('/brands')]);
        if (!active) return;
        setCategories(categoryRes.data || []);
        setBrands(brandRes.data || []);

        if (editing) {
          const productRes = await api.get(`/products/${id}`);
          const data = productRes.data;
          if (!active) return;
          setProduct({
            name: data.name || '',
            description: data.description || '',
            categoryId: data.categoryId || '',
            brandId: data.brandId || '',
            images: data.images?.length
              ? data.images.map((image, index) => ({
                id: image.id,
                url: image.url || '',
                cloudinaryPublicId: image.cloudinaryPublicId || '',
                main: image.main ?? index === 0,
                sortOrder: image.sortOrder ?? index,
              }))
              : (data.imageUrls || []).map((url, index) => ({
                url,
                cloudinaryPublicId: '',
                main: index === 0,
                sortOrder: index,
              })),
          });
          setSkus(data.skus?.length ? data.skus.map((sku) => ({
            skuCode: sku.skuCode || '',
            tierIndex: sku.tierIndex || 'Mặc định',
            price: sku.price || '',
            stock: sku.stock || '',
          })) : [emptySku]);
          setSpecs(data.specs?.length ? data.specs.map((spec, index) => ({
            key: spec.key || '',
            value: spec.value || '',
            displayOrder: index,
          })) : []);
        }
      } catch (err) {
        if (active) setNotice({ type: 'error', text: getApiError(err, 'Không thể tải dữ liệu biểu mẫu.') });
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, [editing, id]);

  const addUploadedImage = (info) => {
    setProduct((current) => ({
      ...current,
      images: [
        ...current.images,
        {
          id: null,
          url: info.secure_url,
          cloudinaryPublicId: info.public_id,
          main: current.images.length === 0,
          sortOrder: current.images.length,
        },
      ],
    }));
  };

  const openCloudinaryWidget = () => {
    openSignedUploadWidget({
      folder: import.meta.env.VITE_CLOUDINARY_PRODUCT_FOLDER || 'ecommerce/products',
      multiple: true,
      maxFiles: 8,
      onSuccess: addUploadedImage,
      setNotice,
    });
  };

  const removeImage = async (imageIndex) => {
    const image = product.images[imageIndex];
    if (!image?.id && image?.cloudinaryPublicId) {
      try {
        await api.post('/admin/cloudinary/delete', { publicId: image.cloudinaryPublicId });
      } catch {
        // Không chặn thao tác gỡ ảnh khỏi form nếu Cloudinary xóa tạm thời thất bại.
      }
    }

    setProduct({ ...product, images: product.images.filter((_, index) => index !== imageIndex) });
  };

  const updateSku = (index, field, value) => {
    const next = [...skus];
    next[index] = { ...next[index], [field]: value };
    setSkus(next);
  };

  const updateSpec = (index, field, value) => {
    const next = [...specs];
    next[index] = { ...next[index], [field]: value };
    setSpecs(next);
  };

  const createQuickCategory = async () => {
    if (!quickCategory.name.trim()) return;
    setQuickSaving((current) => ({ ...current, category: true }));
    setNotice({ type: '', text: '' });
    try {
      const response = await api.post('/categories', {
        name: quickCategory.name.trim(),
        description: quickCategory.description.trim() || null,
        active: true,
      });
      setCategories((current) => [...current, response.data]);
      setProduct((current) => ({ ...current, categoryId: response.data.id }));
      setQuickCategory({ name: '', description: '' });
      setNotice({ type: 'success', text: 'Đã tạo danh mục mới.' });
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể tạo danh mục.') });
    } finally {
      setQuickSaving((current) => ({ ...current, category: false }));
    }
  };

  const createQuickBrand = async () => {
    if (!quickBrand.name.trim()) return;
    setQuickSaving((current) => ({ ...current, brand: true }));
    setNotice({ type: '', text: '' });
    try {
      const response = await api.post('/brands', {
        name: quickBrand.name.trim(),
        description: quickBrand.description.trim() || null,
      });
      setBrands((current) => [...current, response.data]);
      setProduct((current) => ({ ...current, brandId: response.data.id }));
      setQuickBrand({ name: '', description: '' });
      setNotice({ type: 'success', text: 'Đã tạo thương hiệu mới.' });
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể tạo thương hiệu.') });
    } finally {
      setQuickSaving((current) => ({ ...current, brand: false }));
    }
  };

  const submitForm = async (event) => {
    event.preventDefault();
    setSaving(true);
    setNotice({ type: '', text: '' });

    const images = product.images.filter((image) => image.url?.trim());
    const hasMainImage = images.some((image) => image.main);
    const payload = {
      name: product.name.trim(),
      description: product.description.trim(),
      categoryId: Number(product.categoryId),
      brandId: Number(product.brandId),
      images: images.map((image, index) => ({
        url: image.url.trim(),
        cloudinaryPublicId: image.cloudinaryPublicId?.trim() || null,
        main: hasMainImage ? Boolean(image.main) : index === 0,
        sortOrder: Number(image.sortOrder ?? index),
      })),
      skus: skus.map((sku) => ({
        skuCode: sku.skuCode.trim(),
        tierIndex: sku.tierIndex.trim() || 'Mặc định',
        price: Number(sku.price),
        stock: Number(sku.stock),
      })),
      specs: specs
        .filter((spec) => spec.key.trim() && spec.value.trim())
        .map((spec, index) => ({ key: spec.key.trim(), value: spec.value.trim(), displayOrder: Number(spec.displayOrder || index) })),
    };

    try {
      if (editing) {
        await api.put(`/products/admin/${id}`, payload);
      } else {
        await api.post('/products/admin', payload);
      }
      navigate('/admin/products');
    } catch (err) {
      setNotice({ type: 'error', text: getApiError(err, 'Không thể lưu sản phẩm.') });
    } finally {
      setSaving(false);
    }
  };

  return (
    <AdminLayout
      title={editing ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}
      description="Quản lý thông tin bán hàng, ảnh, SKU và thông số kỹ thuật."
      action={<Link to="/admin/products" className="rounded-md border border-gray-300 px-4 py-2 text-sm font-semibold">Quay lại</Link>}
    >
      <Notice type={notice.type} message={notice.text} />
      {loading ? (
        <div className="mt-4 rounded-md border border-gray-200 bg-white p-8 text-sm text-gray-500">Đang tải dữ liệu...</div>
      ) : (
        <form onSubmit={submitForm} className="mt-4 space-y-5">
          <section className="rounded-md border border-gray-200 bg-white p-5">
            <h2 className="font-semibold text-gray-950">Thông tin cơ bản</h2>
            <div className="mt-4 grid gap-4 md:grid-cols-2">
              <input required value={product.name} onChange={(event) => setProduct({ ...product, name: event.target.value })} placeholder="Tên sản phẩm" className="rounded-md border border-gray-300 px-3 py-2 text-sm" />
              <div className="space-y-2">
                <select required value={product.categoryId} onChange={(event) => setProduct({ ...product, categoryId: event.target.value })} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm">
                  <option value="">Chọn danh mục</option>
                  {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
                </select>
                <div className="rounded-md border border-dashed border-gray-300 p-3">
                  <div className="grid gap-2 md:grid-cols-[1fr_auto]">
                    <input value={quickCategory.name} onChange={(event) => setQuickCategory({ ...quickCategory, name: event.target.value })} placeholder="Tên danh mục mới" className="rounded-md border border-gray-300 px-3 py-2 text-sm" />
                    <button type="button" onClick={createQuickCategory} disabled={quickSaving.category || !quickCategory.name.trim()} className="rounded-md border border-primary px-3 py-2 text-sm font-semibold text-primary disabled:opacity-50">
                      {quickSaving.category ? 'Đang tạo...' : 'Tạo nhanh'}
                    </button>
                  </div>
                  <input value={quickCategory.description} onChange={(event) => setQuickCategory({ ...quickCategory, description: event.target.value })} placeholder="Mô tả danh mục" className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
                </div>
              </div>
              <div className="space-y-2">
                <select required value={product.brandId} onChange={(event) => setProduct({ ...product, brandId: event.target.value })} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm">
                  <option value="">Chọn thương hiệu</option>
                  {brands.map((brand) => <option key={brand.id} value={brand.id}>{brand.name}</option>)}
                </select>
                <div className="rounded-md border border-dashed border-gray-300 p-3">
                  <div className="grid gap-2 md:grid-cols-[1fr_auto]">
                    <input value={quickBrand.name} onChange={(event) => setQuickBrand({ ...quickBrand, name: event.target.value })} placeholder="Tên thương hiệu mới" className="rounded-md border border-gray-300 px-3 py-2 text-sm" />
                    <button type="button" onClick={createQuickBrand} disabled={quickSaving.brand || !quickBrand.name.trim()} className="rounded-md border border-primary px-3 py-2 text-sm font-semibold text-primary disabled:opacity-50">
                      {quickSaving.brand ? 'Đang tạo...' : 'Tạo nhanh'}
                    </button>
                  </div>
                  <input value={quickBrand.description} onChange={(event) => setQuickBrand({ ...quickBrand, description: event.target.value })} placeholder="Mô tả thương hiệu" className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
                </div>
              </div>
              <textarea value={product.description} onChange={(event) => setProduct({ ...product, description: event.target.value })} placeholder="Mô tả sản phẩm" rows="4" className="rounded-md border border-gray-300 px-3 py-2 text-sm md:col-span-2" />
            </div>
          </section>

          <section className="rounded-md border border-gray-200 bg-white p-5">
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="font-semibold text-gray-950">Ảnh sản phẩm</h2>
              </div>
              <button type="button" onClick={openCloudinaryWidget} className="rounded-md bg-primary px-3 py-2 text-sm font-semibold text-white hover:bg-primary-dark">
                Tải ảnh lên
              </button>
            </div>
            <div className="mt-4 grid gap-3 md:grid-cols-2">
              {product.images.length === 0 && <p className="text-sm text-gray-500">Chưa có ảnh sản phẩm.</p>}
              {product.images.map((image, index) => (
                <div key={`${image.url}-${index}`} className="rounded-md border border-gray-200 p-3">
                  <img src={image.url || 'https://via.placeholder.com/300'} alt={`Ảnh sản phẩm ${index + 1}`} className="aspect-video w-full rounded-md object-cover" />
                  <div className="mt-3 space-y-2">
                    <label className="flex items-center gap-2 text-sm text-gray-700">
                      <input
                        type="radio"
                        checked={Boolean(image.main)}
                        onChange={() => setProduct({
                          ...product,
                          images: product.images.map((current, currentIndex) => ({ ...current, main: currentIndex === index })),
                        })}
                      />
                      Ảnh chính
                    </label>
                    <button type="button" onClick={() => removeImage(index)} className="rounded-md border border-red-200 px-3 py-2 text-sm font-semibold text-red-600">Xóa ảnh</button>
                  </div>
                </div>
              ))}
            </div>
          </section>

          <section className="rounded-md border border-gray-200 bg-white p-5">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold text-gray-950">SKU và tồn kho</h2>
              <button type="button" onClick={() => setSkus([...skus, emptySku])} className="text-sm font-semibold text-primary">Thêm SKU</button>
            </div>
            <div className="mt-4 overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-gray-50 text-gray-500">
                  <tr>
                    <th className="px-3 py-2">Phân loại</th>
                    <th className="px-3 py-2">Mã SKU</th>
                    <th className="px-3 py-2">Giá</th>
                    <th className="px-3 py-2">Tồn kho</th>
                    <th className="px-3 py-2"></th>
                  </tr>
                </thead>
                <tbody>
                  {skus.map((sku, index) => (
                    <tr key={index}>
                      <td className="px-3 py-2"><input required value={sku.tierIndex} onChange={(event) => updateSku(index, 'tierIndex', event.target.value)} className="w-full rounded-md border border-gray-300 px-3 py-2" /></td>
                      <td className="px-3 py-2"><input required value={sku.skuCode} onChange={(event) => updateSku(index, 'skuCode', event.target.value)} className="w-full rounded-md border border-gray-300 px-3 py-2" /></td>
                      <td className="px-3 py-2"><input required min="1" type="number" value={sku.price} onChange={(event) => updateSku(index, 'price', event.target.value)} className="w-full rounded-md border border-gray-300 px-3 py-2" /></td>
                      <td className="px-3 py-2"><input required min="0" type="number" value={sku.stock} onChange={(event) => updateSku(index, 'stock', event.target.value)} className="w-full rounded-md border border-gray-300 px-3 py-2" /></td>
                      <td className="px-3 py-2">{skus.length > 1 && <button type="button" onClick={() => setSkus(skus.filter((_, i) => i !== index))} className="font-semibold text-red-600">Xóa</button>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          <section className="rounded-md border border-gray-200 bg-white p-5">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold text-gray-950">Thông số kỹ thuật</h2>
              <button type="button" onClick={() => setSpecs([...specs, { ...emptySpec, displayOrder: specs.length }])} className="text-sm font-semibold text-primary">Thêm thông số</button>
            </div>
            <div className="mt-4 space-y-3">
              {specs.length === 0 && <p className="text-sm text-gray-500">Chưa có thông số kỹ thuật.</p>}
              {specs.map((spec, index) => (
                <div key={index} className="grid gap-2 md:grid-cols-[1fr_1fr_auto]">
                  <input value={spec.key} onChange={(event) => updateSpec(index, 'key', event.target.value)} placeholder="Tên thông số, ví dụ RAM" className="rounded-md border border-gray-300 px-3 py-2 text-sm" />
                  <input value={spec.value} onChange={(event) => updateSpec(index, 'value', event.target.value)} placeholder="Giá trị, ví dụ 8 GB" className="rounded-md border border-gray-300 px-3 py-2 text-sm" />
                  <button type="button" onClick={() => setSpecs(specs.filter((_, i) => i !== index))} className="rounded-md border border-red-200 px-3 py-2 text-sm font-semibold text-red-600">Xóa</button>
                </div>
              ))}
            </div>
          </section>

          <div className="flex justify-end gap-3">
            <Link to="/admin/products" className="rounded-md border border-gray-300 px-4 py-2 text-sm font-semibold">Hủy</Link>
            <button disabled={saving} className="rounded-md bg-primary px-5 py-2 text-sm font-semibold text-white hover:bg-primary-dark disabled:opacity-60">
              {saving ? 'Đang lưu...' : 'Lưu sản phẩm'}
            </button>
          </div>
        </form>
      )}
    </AdminLayout>
  );
};

export default ProductForm;
