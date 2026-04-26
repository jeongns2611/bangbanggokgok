type CompareSectionProps = {
  title: string;
  children: React.ReactNode;
  headerBgColor?: string;
};

export default function CompareSection({
  title,
  children,
  headerBgColor = 'bg-[#bfd8c7]',
}: CompareSectionProps) {
  return (
    <section className="overflow-hidden rounded-2xl border border-[#d9e7dc] bg-white shadow-sm">
      <div className={`${headerBgColor} px-5 py-4`}>
        <h3 className="text-base font-semibold text-[#1f3b2c]">{title}</h3>
      </div>
      <div className="px-5 py-5">{children}</div>
    </section>
  );
}