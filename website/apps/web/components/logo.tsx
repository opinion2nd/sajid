import Image from "next/image";
import Link from "next/link";
import { cn } from "@/lib/utils";

export function Logo({
  className,
  showText = true,
  size = 40,
}: {
  className?: string;
  showText?: boolean;
  size?: number;
}) {
  return (
    <Link href="/" className={cn("flex shrink-0 items-center gap-2", className)}>
      <Image
        src="/logo.jpg"
        alt="Brother Craft"
        width={size}
        height={size}
        priority
        className="rounded-lg"
      />
      {showText && (
        <span className="text-lg font-bold tracking-tight">
          Brother<span className="text-accent">Craft</span>
        </span>
      )}
    </Link>
  );
}
