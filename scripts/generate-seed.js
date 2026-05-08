#!/usr/bin/env node
/**
 * Generate scripts/seed.sql with realistic Vietnamese sample data for booking_app.
 *
 * Counts:
 *   users 50, properties 30, rooms 80, property_images 60, room_images 80,
 *   room_inventory 80 rooms x 14 days = 1120, bookings 60, payments 45, reviews 15
 */

const fs = require('fs');
const path = require('path');

// BCrypt hash for "Password123!"
const PASSWORD_HASH = '$2b$10$UwsCXzyGyIFvKkG7Rk4NVeQkZREVDIWf10rLcL/plRZa/Uk4MobAe';

const surnames = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Vũ', 'Đặng', 'Bùi', 'Đỗ', 'Hồ', 'Ngô', 'Dương', 'Lý'];
const middlesM = ['Văn', 'Minh', 'Quốc', 'Hữu', 'Đức', 'Anh', 'Tuấn', 'Thanh'];
const middlesF = ['Thị', 'Thu', 'Thanh', 'Ngọc', 'Mỹ', 'Diệu', 'Phương'];
const givenM = ['An', 'Bảo', 'Cường', 'Dũng', 'Hùng', 'Khánh', 'Long', 'Nam', 'Phúc', 'Quân', 'Sơn', 'Tâm', 'Tuấn', 'Việt', 'Hải', 'Khoa', 'Phát', 'Trung', 'Hiếu', 'Đạt'];
const givenF = ['An', 'Bích', 'Châu', 'Dung', 'Hà', 'Hằng', 'Hương', 'Lan', 'Linh', 'Mai', 'Ngân', 'Nhung', 'Oanh', 'Phương', 'Quỳnh', 'Thảo', 'Trang', 'Vân', 'Yến', 'Hoa'];

const cities = [
  { city: 'Đà Lạt', country: 'Vietnam', addresses: ['Đường Trần Phú', 'Đường Nguyễn Văn Cừ', 'Đường Hồ Tùng Mậu', 'Đường Phan Đình Phùng', 'Đường Bùi Thị Xuân'] },
  { city: 'Đà Nẵng', country: 'Vietnam', addresses: ['Đường Bạch Đằng', 'Đường Võ Nguyên Giáp', 'Đường Trần Phú', 'Đường Phan Châu Trinh', 'Đường Hoàng Sa'] },
  { city: 'Hội An', country: 'Vietnam', addresses: ['Đường Bạch Đằng', 'Đường Trần Hưng Đạo', 'Đường Cửa Đại', 'Đường Nguyễn Phúc Chu', 'Đường Lý Thái Tổ'] },
  { city: 'Sa Pa', country: 'Vietnam', addresses: ['Đường Mường Hoa', 'Đường Cầu Mây', 'Đường Fansipan', 'Đường Thác Bạc', 'Đường Xuân Viên'] },
  { city: 'Phú Quốc', country: 'Vietnam', addresses: ['Đường Trần Hưng Đạo', 'Đường Bãi Sao', 'Đường Bãi Trường', 'Đường Dương Đông', 'Đường Bãi Khem'] },
  { city: 'Hà Giang', country: 'Vietnam', addresses: ['Đường Nguyễn Trãi', 'Đường Đồng Văn', 'Đường Mèo Vạc', 'Đường Quản Bạ', 'Đường Yên Minh'] },
  { city: 'Mai Châu', country: 'Vietnam', addresses: ['Bản Lác', 'Bản Pom Coọng', 'Bản Văn', 'Đường QL15', 'Đường Mai Hịch'] },
];

const propertyNamesByCity = {
  'Đà Lạt': ['Sunset Villa Đà Lạt', 'Pine Hill Homestay', 'Dalat Flower Garden', 'Eco Valley Đà Lạt', 'Sương Mai Homestay', 'Đồi Mộng Mơ Lodge', 'Forest View Villa', 'Lavender House'],
  'Đà Nẵng': ['Beachfront Đà Nẵng', 'My Khe Sea Villa', 'Sơn Trà Boutique', 'Han River Homestay', 'Bãi Bụt Resort', 'Marble Mountain Stay'],
  'Hội An': ['Hội An Riverside', 'Old Town Garden', 'An Bang Beach House', 'Cẩm Thanh Coconut', 'Hội An Lantern Stay', 'Faifo Heritage Homestay'],
  'Sa Pa': ['Sa Pa Cloud View', 'H\'mong Eco Lodge', 'Mường Hoa Valley', 'Fansipan Highland', 'Cát Cát Village Stay'],
  'Phú Quốc': ['Phú Quốc Pearl Villa', 'Sao Beach Bungalow', 'Sunset Sanato', 'Long Beach Resort', 'Bãi Khem Hideaway'],
  'Hà Giang': ['Đồng Văn Stone House', 'Hà Giang Sky Lodge', 'Mèo Vạc Highland', 'Quản Bạ Eco Stay'],
  'Mai Châu': ['Mai Châu Ecolodge', 'Bản Lác Stilt House', 'Pom Coọng Homestay', 'Mai Hịch Valley'],
};

const roomTypes = [
  { type: 'Standard', cap: 2, priceMin: 300000, priceMax: 600000 },
  { type: 'Deluxe', cap: 2, priceMin: 600000, priceMax: 1200000 },
  { type: 'Family', cap: 4, priceMin: 1000000, priceMax: 1800000 },
  { type: 'Dorm', cap: 6, priceMin: 200000, priceMax: 350000 },
  { type: 'Suite', cap: 4, priceMin: 1500000, priceMax: 2500000 },
];

const reviewComments = [
  'Phòng sạch sẽ, view đẹp, chủ nhà nhiệt tình. Sẽ quay lại lần nữa!',
  'Vị trí thuận tiện, nhân viên thân thiện. Bữa sáng ngon.',
  'Không gian ấm cúng, đúng chất homestay. Rất đáng tiền.',
  'Phòng rộng rãi, đầy đủ tiện nghi. Mình rất hài lòng.',
  'Chủ nhà tâm lý, giới thiệu nhiều địa điểm hay. Cảnh quan tuyệt vời.',
  'Yên tĩnh, sạch sẽ, phù hợp cho gia đình. Đáng đồng tiền.',
  'Phòng đẹp như ảnh, dịch vụ tốt. Recommended!',
  'Trải nghiệm tuyệt vời, sẽ giới thiệu cho bạn bè.',
  'Đồ ăn ngon, vị trí trung tâm, đi lại thuận tiện.',
  'Decor đẹp, có nhiều góc sống ảo. Chủ rất friendly.',
  'View núi rất đẹp vào buổi sáng. Phòng ấm vào ban đêm.',
  'Không gian xanh, thư giãn. Phù hợp nghỉ dưỡng cuối tuần.',
  'Cơ sở vật chất tốt, wifi ổn. Có dịch vụ thuê xe máy.',
  'Khu vực an ninh, sạch sẽ. Gần bãi biển, rất tiện.',
  'Trải nghiệm văn hóa địa phương đáng nhớ. Cảm ơn chủ nhà!',
];

// ---------- helpers ----------
const rand = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;
const pick = (arr) => arr[rand(0, arr.length - 1)];
const sqlStr = (s) => `'${String(s).replace(/'/g, "''")}'`;

function vietnamName(gender) {
  const sn = pick(surnames);
  if (gender === 'M') return `${sn} ${pick(middlesM)} ${pick(givenM)}`;
  return `${sn} ${pick(middlesF)} ${pick(givenF)}`;
}

function emailize(name, idx, domain) {
  const norm = name.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/gi, 'd').toLowerCase().replace(/\s+/g, '.');
  return `${norm}${idx}@${domain}`;
}

function vnPhone() {
  const prefixes = ['090', '091', '093', '094', '096', '097', '098', '032', '033', '034', '035', '036', '037', '038', '039', '070', '076', '077', '078', '079', '081', '082', '083', '084', '085', '086', '088', '089'];
  return pick(prefixes) + String(rand(1000000, 9999999));
}

function dateOffset(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
}

function datetimeOffset(daysOffset, hour) {
  const d = new Date();
  d.setDate(d.getDate() + daysOffset);
  d.setHours(hour, rand(0, 59), rand(0, 59), 0);
  return d.toISOString().slice(0, 19).replace('T', ' ');
}

// ---------- generation ----------
let lines = [];
const W = (s) => lines.push(s);

W('-- ============================================================');
W('-- booking_app seed data (Vietnamese, realistic)');
W('-- Generated: ' + new Date().toISOString());
W('-- Password for all users: Password123!');
W('-- BCrypt hash: ' + PASSWORD_HASH);
W('-- ============================================================');
W('SET NAMES utf8mb4;');
W('SET FOREIGN_KEY_CHECKS = 0;');
W('TRUNCATE TABLE reviews;');
W('TRUNCATE TABLE payments;');
W('TRUNCATE TABLE bookings;');
W('TRUNCATE TABLE room_inventory;');
W('TRUNCATE TABLE room_images;');
W('TRUNCATE TABLE property_images;');
W('TRUNCATE TABLE rooms;');
W('TRUNCATE TABLE properties;');
W('TRUNCATE TABLE users;');
W('SET FOREIGN_KEY_CHECKS = 1;');
W('');

// USERS: 5 ADMIN, 15 HOST, 30 GUEST = 50
W('-- ===== users (50) =====');
const users = [];
let uid = 1;
const roles = [
  { role: 'ADMIN', count: 5, domain: 'stay.vn', prefix: 'admin' },
  { role: 'HOST', count: 15, domain: 'stay.vn', prefix: 'host' },
  { role: 'GUEST', count: 30, domain: 'gmail.com', prefix: null },
];

const userValues = [];
for (const r of roles) {
  for (let i = 1; i <= r.count; i++) {
    const gender = Math.random() < 0.5 ? 'M' : 'F';
    const fullName = vietnamName(gender);
    const email = r.prefix
      ? `${r.prefix}${String(i).padStart(2, '0')}@${r.domain}`
      : emailize(fullName, i, r.domain);
    const phone = vnPhone();
    users.push({ id: uid, email, fullName, phone, role: r.role });
    userValues.push(`(${uid}, ${sqlStr(email)}, ${sqlStr(PASSWORD_HASH)}, ${sqlStr(fullName)}, ${sqlStr(phone)}, ${sqlStr(r.role)})`);
    uid++;
  }
}
W('INSERT INTO users (id, email, password_hash, full_name, phone_number, role) VALUES');
W(userValues.join(',\n') + ';');
W('');

// PROPERTIES: 30 — distributed across 15 hosts, each owning 1-3 properties
W('-- ===== properties (30) =====');
const hostIds = users.filter(u => u.role === 'HOST').map(u => u.id);
const properties = [];
let pid = 1;

// Distribute 30 properties across 15 hosts
const hostLoad = {};
hostIds.forEach(h => hostLoad[h] = 0);
const usedNames = new Set();

while (properties.length < 30) {
  const host = pick(hostIds);
  if (hostLoad[host] >= 3) continue;
  const cityObj = pick(cities);
  const cityProps = propertyNamesByCity[cityObj.city];
  let name;
  for (let attempt = 0; attempt < 20; attempt++) {
    name = pick(cityProps);
    if (!usedNames.has(name)) break;
  }
  if (usedNames.has(name)) name = `${name} ${properties.length + 1}`;
  usedNames.add(name);
  const address = `${rand(1, 300)} ${pick(cityObj.addresses)}, ${cityObj.city}`;
  const description = `${name} - homestay phong cách hiện đại tại ${cityObj.city}. Không gian thoáng đãng, view đẹp, gần các điểm du lịch nổi tiếng. Phù hợp cho gia đình, cặp đôi và nhóm bạn.`;
  properties.push({ id: pid, hostId: host, name, address, city: cityObj.city, country: cityObj.country, description });
  hostLoad[host]++;
  pid++;
}

const propertyValues = properties.map(p =>
  `(${p.id}, ${p.hostId}, ${sqlStr(p.name)}, ${sqlStr(p.description)}, ${sqlStr(p.address)}, ${sqlStr(p.city)}, ${sqlStr(p.country)}, 1)`
);
W('INSERT INTO properties (id, host_id, name, description, address, city, country, is_active) VALUES');
W(propertyValues.join(',\n') + ';');
W('');

// ROOMS: 80 — each property gets 2-4 rooms
W('-- ===== rooms (80) =====');
const rooms = [];
let rid = 1;
const baseRoomsPerProperty = 2;
const extraRoomsTotal = 80 - baseRoomsPerProperty * properties.length; // 20 extra
const extraTargets = new Set();
while (extraTargets.size < extraRoomsTotal) extraTargets.add(rand(1, properties.length));

for (const prop of properties) {
  const roomsCount = baseRoomsPerProperty + (extraTargets.has(prop.id) ? rand(1, 2) : 0);
  // shuffle roomTypes copy
  const shuffled = [...roomTypes].sort(() => Math.random() - 0.5);
  for (let i = 0; i < roomsCount && rooms.length < 80; i++) {
    const rt = shuffled[i % shuffled.length];
    const price = rand(rt.priceMin, rt.priceMax);
    // round to nearest 50000
    const rounded = Math.round(price / 50000) * 50000;
    const quantity = rand(2, 5);
    rooms.push({ id: rid, propertyId: prop.id, roomType: rt.type, capacity: rt.cap, basePrice: rounded, quantity });
    rid++;
  }
}
// trim/extend to exactly 80
while (rooms.length < 80) {
  const prop = pick(properties);
  const rt = pick(roomTypes);
  const price = Math.round(rand(rt.priceMin, rt.priceMax) / 50000) * 50000;
  rooms.push({ id: rid, propertyId: prop.id, roomType: rt.type, capacity: rt.cap, basePrice: price, quantity: rand(2, 5) });
  rid++;
}
const roomValues = rooms.map(r =>
  `(${r.id}, ${r.propertyId}, ${sqlStr(r.roomType)}, ${r.capacity}, ${r.basePrice}, ${r.quantity})`
);
W('INSERT INTO rooms (id, property_id, room_type, capacity, base_price, quantity) VALUES');
W(roomValues.join(',\n') + ';');
W('');

// PROPERTY_IMAGES: 60 — 2 per property (1 thumbnail + 1 normal)
W('-- ===== property_images (60) =====');
let pimgId = 1;
const propImgValues = [];
for (const p of properties) {
  for (let i = 0; i < 2; i++) {
    const url = `https://res.cloudinary.com/dvx8eohft/image/upload/v1700000000/sample_property_${p.id}_${i + 1}.jpg`;
    const publicId = `properties/property_${p.id}_${i + 1}`;
    propImgValues.push(`(${pimgId}, ${p.id}, ${sqlStr(url)}, ${sqlStr(publicId)}, ${i === 0 ? 1 : 0})`);
    pimgId++;
  }
}
W('INSERT INTO property_images (id, property_id, image_url, public_id, is_thumbnail) VALUES');
W(propImgValues.join(',\n') + ';');
W('');

// ROOM_IMAGES: 80 — 1 per room (thumbnail)
W('-- ===== room_images (80) =====');
let rimgId = 1;
const roomImgValues = rooms.map(r => {
  const url = `https://res.cloudinary.com/dvx8eohft/image/upload/v1700000000/sample_room_${r.id}.jpg`;
  const publicId = `rooms/room_${r.id}`;
  const v = `(${rimgId}, ${r.id}, ${sqlStr(url)}, ${sqlStr(publicId)}, 1)`;
  rimgId++;
  return v;
});
W('INSERT INTO room_images (id, room_id, image_url, public_id, is_thumbnail) VALUES');
W(roomImgValues.join(',\n') + ';');
W('');

// ROOM_INVENTORY: 80 rooms × 14 days = 1120
W('-- ===== room_inventory (80 rooms x 14 days = 1120) =====');
let invId = 1;
const invValues = [];
for (const r of rooms) {
  for (let day = 0; day < 14; day++) {
    invValues.push(`(${invId}, ${r.id}, '${dateOffset(day)}', ${r.quantity})`);
    invId++;
  }
}
// Split inserts in chunks of 200 to avoid huge single INSERT
W('INSERT INTO room_inventory (id, room_id, inventory_date, available_count) VALUES');
const invChunks = [];
for (let i = 0; i < invValues.length; i += 200) {
  invChunks.push(invValues.slice(i, i + 200).join(',\n'));
}
W(invChunks.join(';\nINSERT INTO room_inventory (id, room_id, inventory_date, available_count) VALUES\n') + ';');
W('');

// BOOKINGS: 60 — 10 PENDING, 25 CONFIRMED, 20 COMPLETED, 5 CANCELLED
W('-- ===== bookings (60) =====');
const guestIds = users.filter(u => u.role === 'GUEST').map(u => u.id);
const bookings = [];
let bid = 1;

const statusPlan = [
  ...Array(10).fill('PENDING'),
  ...Array(25).fill('CONFIRMED'),
  ...Array(20).fill('COMPLETED'),
  ...Array(5).fill('CANCELLED'),
];

for (const status of statusPlan) {
  const room = pick(rooms);
  const guest = pick(guestIds);
  const nights = rand(1, 5);
  const roomQty = rand(1, Math.min(2, room.quantity));
  let checkIn, checkOut, createdAt;

  if (status === 'COMPLETED') {
    const offset = -rand(7, 60);
    checkIn = dateOffset(offset);
    checkOut = dateOffset(offset + nights);
    createdAt = datetimeOffset(offset - rand(3, 30), rand(8, 22));
  } else if (status === 'CONFIRMED') {
    const offset = rand(1, 50);
    checkIn = dateOffset(offset);
    checkOut = dateOffset(offset + nights);
    createdAt = datetimeOffset(-rand(0, 14), rand(8, 22));
  } else if (status === 'PENDING') {
    const offset = rand(2, 30);
    checkIn = dateOffset(offset);
    checkOut = dateOffset(offset + nights);
    createdAt = datetimeOffset(-rand(0, 13), rand(8, 22));
  } else { // CANCELLED
    const offset = rand(-30, 30);
    checkIn = dateOffset(offset);
    checkOut = dateOffset(offset + nights);
    createdAt = datetimeOffset(offset - rand(2, 20), rand(8, 22));
  }

  const totalPrice = room.basePrice * roomQty * nights;
  bookings.push({ id: bid, guestId: guest, roomId: room.id, checkIn, checkOut, totalPrice, roomQty, status, createdAt });
  bid++;
}

const bookingValues = bookings.map(b =>
  `(${b.id}, ${b.guestId}, ${b.roomId}, '${b.checkIn}', '${b.checkOut}', ${b.totalPrice}, '${b.status}', '${b.createdAt}', ${b.roomQty})`
);
W('INSERT INTO bookings (id, guest_id, room_id, check_in_date, check_out_date, total_price, status, created_at, room_quantity) VALUES');
W(bookingValues.join(',\n') + ';');
W('');

// PAYMENTS: 45 — for all CONFIRMED + COMPLETED (25 + 20 = 45)
W('-- ===== payments (45) =====');
const paidBookings = bookings.filter(b => b.status === 'CONFIRMED' || b.status === 'COMPLETED');
let payId = 1;
const paymentValues = paidBookings.map(b => {
  const txn = 'VNP' + String(rand(10000000000000, 99999999999999));
  const v = `(${payId}, ${b.id}, ${b.totalPrice}, 'VNPAY', 'SUCCESS', '${txn}', '${b.createdAt}')`;
  payId++;
  return v;
});
W('INSERT INTO payments (id, booking_id, amount, payment_method, status, transaction_id, created_at) VALUES');
W(paymentValues.join(',\n') + ';');
W('');

// REVIEWS: 15 — for first 15 COMPLETED bookings
W('-- ===== reviews (15) =====');
const completedBookings = bookings.filter(b => b.status === 'COMPLETED').slice(0, 15);
let revId = 1;
const reviewValues = completedBookings.map(b => {
  const room = rooms.find(r => r.id === b.roomId);
  const propertyId = room.propertyId;
  const rating = rand(3, 5);
  const comment = pick(reviewComments);
  // review created shortly after checkout
  const checkoutDate = new Date(b.checkOut);
  checkoutDate.setDate(checkoutDate.getDate() + rand(1, 7));
  checkoutDate.setHours(rand(8, 22), rand(0, 59), 0, 0);
  const createdAt = checkoutDate.toISOString().slice(0, 19).replace('T', ' ');
  const v = `(${revId}, ${b.id}, ${propertyId}, ${rating}, ${sqlStr(comment)}, '${createdAt}')`;
  revId++;
  return v;
});
W('INSERT INTO reviews (id, booking_id, property_id, rating, comment, created_at) VALUES');
W(reviewValues.join(',\n') + ';');
W('');

W('-- ===== summary report =====');
W("SELECT 'users' AS tbl, COUNT(*) AS rows_inserted FROM users");
W("UNION ALL SELECT 'properties', COUNT(*) FROM properties");
W("UNION ALL SELECT 'rooms', COUNT(*) FROM rooms");
W("UNION ALL SELECT 'property_images', COUNT(*) FROM property_images");
W("UNION ALL SELECT 'room_images', COUNT(*) FROM room_images");
W("UNION ALL SELECT 'room_inventory', COUNT(*) FROM room_inventory");
W("UNION ALL SELECT 'bookings', COUNT(*) FROM bookings");
W("UNION ALL SELECT 'payments', COUNT(*) FROM payments");
W("UNION ALL SELECT 'reviews', COUNT(*) FROM reviews;");

const out = path.join(__dirname, 'seed.sql');
fs.writeFileSync(out, lines.join('\n'), 'utf8');
console.log(`Wrote ${out} (${lines.length} lines)`);
