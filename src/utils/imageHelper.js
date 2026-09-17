/**
 * 图片助手工具
 * 文件路径: src/utils/imageHelper.js
 * 功能: 统一处理图片路径
 */

/**
 * 获取商品图片URL
 * @param {Object|string} product - 商品对象或图片路径
 * @param {number} index - 图片索引（多图时使用）
 * @returns {string} 图片URL
 */
export function getProductImage(product, index = 0) {
    // 如果是字符串，直接使用
    if (typeof product === 'string') {
        return processImagePath(product);
    }

    // 如果是对象
    if (product && typeof product === 'object') {
        // 如果有 images 数组
        if (product.images && Array.isArray(product.images) && product.images.length > 0) {
            return processImagePath(product.images[index] || product.images[0]);
        }

        // 如果有 image
        if (product.image) {
            return processImagePath(product.image);
        }

        if (product.productImage) {
            return processImagePath(product.productImage);
        }
    }

    // 默认图片
    return '/images/products/default.jpg';
}

/**
 * 获取商家头像URL
 * @param {string} avatarPath - 头像路径
 * @returns {string} 头像URL
 */
export function getMerchantAvatar(avatarPath) {
    if (!avatarPath) {
        return '/images/avatars/default-merchant.jpg';
    }
    return processImagePath(avatarPath);
}

/**
 * 处理图片路径
 * @param {string} imgPath - 原始图片路径
 * @returns {string} 处理后的路径
 */
function processImagePath(imgPath) {
    if (!imgPath) return '/images/default.jpg';

    // 如果是完整URL
    if (imgPath.startsWith('http')) {
        return imgPath;
    }

    // 如果是 @/assets/ 路径
    if (imgPath.startsWith('@/assets/')) {
        const fileName = imgPath.split('/').pop();
        if (imgPath.includes('avatars')) {
            return `/images/avatars/${fileName}`;
        } else if (imgPath.includes('products')) {
            return `/images/products/${fileName}`;
        } else if (imgPath.includes('banner')) {
            return `/images/banner/${fileName}`;
        }
        return `/images/${fileName}`;
    }

    // 如果已经是 /images/ 开头的路径
    if (imgPath.startsWith('/images/')) {
        return imgPath;
    }

    // 如果是直接的文件名（如 zhangsan_avatar.png），视为用户头像
    if (imgPath.includes('.') && !imgPath.includes('/')) {
        // 对于直接文件名，返回正确的资源路径
        return require(`../assets/imgs/user/${imgPath}`);
    }

    // 其他情况，返回原路径
    return imgPath;
}

/**
 * 获取轮播图图片
 * @param {string} imgName - 图片名称
 * @returns {string} 图片URL
 */
export function getCarouselImage(imgName) {
    return `/images/banner/${imgName}`;
}

/**
 * 获取产品分类图标
 * @param {string} category - 分类名称
 * @returns {string} 图标类名
 */
export function getCategoryIcon(category) {
    const icons = {
        agricultural: 'el-icon-s-marketing',
        livestock: 'el-icon-s-custom',
        processed: 'el-icon-s-goods'
    };
    return icons[category] || 'el-icon-s-goods';
}

/**
 * 获取默认商品图片
 * @returns {string} 默认图片URL
 */
export function getDefaultProductImage() {
    return '/images/products/default.jpg';
}