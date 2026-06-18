import Link from "next/link";

export default function NotFound() {
  return (
    <div className="container-page flex flex-col items-center justify-center py-24 text-center">
      <p className="text-6xl font-extrabold text-accent">404</p>
      <h1 className="mt-3 text-2xl font-bold">Page not found</h1>
      <p className="mt-1 text-muted">
        The page you&apos;re looking for doesn&apos;t exist or has moved.
      </p>
      <Link href="/" className="btn-primary mt-6">
        Back to home
      </Link>
    </div>
  );
}
