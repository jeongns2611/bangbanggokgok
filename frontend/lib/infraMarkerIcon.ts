const DEFAULT_MARKER = {
  bg: '#FFFFFF',
  border: '#D1D5DB',
  emoji: '📍',
};

type MarkerSpec = {
  bg: string;
  border: string;
  emoji: string;
};

const getInfraMarkerSpec = (type: string): MarkerSpec => {
  switch (type) {
    case '지하철':
      return {
        bg: '#F0FDF4',
        border: '#A7F3D0',
        emoji: '🚇',
      };
    case '버스정류장':
      return {
        bg: '#EFF6FF',
        border: '#BFDBFE',
        emoji: '🚏',
      };
    case '편의점':
      return {
        bg: '#ECFDF5',
        border: '#A7F3D0',
        emoji: '🏪',
      };
    case '세탁소':
      return {
        bg: '#F0F9FF',
        border: '#BAE6FD',
        emoji: '🧺',
      };
    case '카페':
      return {
        bg: '#FFFBEB',
        border: '#FDE68A',
        emoji: '☕',
      };
    case '병원':
      return {
        bg: '#FEF2F2',
        border: '#FECACA',
        emoji: '🏥',
      };
    case '약국':
      return {
        bg: '#F0FDF4',
        border: '#BBF7D0',
        emoji: '💊',
      };
    case 'CCTV':
      return {
        bg: '#F3F4F6',
        border: '#D1D5DB',
        emoji: '📹',
      };
    case '경찰서':
      return {
        bg: '#EFF6FF',
        border: '#BFDBFE',
        emoji: '🚓',
      };
    default:
      return DEFAULT_MARKER;
  }
};

export const getInfraMarkerHtml = (type: string) => {
  const spec = getInfraMarkerSpec(type);

  return `
    <div style="
      width:28px;
      height:28px;
      display:flex;
      align-items:center;
      justify-content:center;
      border-radius:9999px;
      background:${spec.bg};
      border:1px solid ${spec.border};
      box-shadow:0 2px 8px rgba(0,0,0,0.14);
      font-size:14px;
      line-height:1;
      user-select:none;
    ">
      ${spec.emoji}
    </div>
  `;
};
