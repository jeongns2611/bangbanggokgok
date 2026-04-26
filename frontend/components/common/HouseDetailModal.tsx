import React from "react";

interface Image {
  image_url?: string;
  is_thumbnail?: boolean;
}

interface HouseDetailProps {
  open: boolean;
  onClose: () => void;
  house: any | null;
}

const HouseDetailModal: React.FC<HouseDetailProps> = ({ open, onClose, house }) => {
  if (!open || !house) return null;

  return (
    <div
      style={{
        position: "fixed",
        top: 0, left: 0, right: 0, bottom: 0,
        background: "rgba(0,0,0,0.4)",
        zIndex: 1000,
        display: "flex",
        alignItems: "center",
        justifyContent: "center"
      }}
      onClick={onClose}
    >
      <div
        style={{
          background: "#fff",
          padding: 24,
          borderRadius: 8,
          minWidth: 400,
          maxWidth: 600,
          maxHeight: "80vh",
          overflowY: "auto"
        }}
        onClick={e => e.stopPropagation()}
      >
        <button onClick={onClose} style={{ float: "right" }}>닫기</button>
        <h2>{house.sidoName} {house.sigunguName} {house.dongName}</h2>
        <p>{house.address}</p>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
          {house.images && house.images.length > 0 ? (
            house.images.map((img: Image, idx: number) => (
              <img
                key={idx}
                src={img.image_url}
                alt={`매물 이미지 ${idx + 1}`}
                style={{ width: 120, height: 90, objectFit: "cover", borderRadius: 4 }}
              />
            ))
          ) : (
            <span>이미지 없음</span>
          )}
        </div>
        <ul>
          <li>유형: {house.houseType}</li>
          <li>거래유형: {house.rentType}</li>
          <li>상태: {house.houseStatus}</li>
          <li>층: {house.floor}</li>
          <li>보증금: {house.deposit}만원</li>
          <li>월세: {house.monthlyCost}만원</li>
          <li>관리비: {house.managementCost ?? 0}만원</li>
          <li>관리비 항목: {house.managementItems}</li>
          <li>연면적: {house.floorSize}㎡</li>
          <li>건축년도: {house.buildYear}</li>
        </ul>
      </div>
    </div>
  );
};

export default HouseDetailModal;
