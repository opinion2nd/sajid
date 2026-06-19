export async function fetchActionGif(reaction: string): Promise<string | null> {
  try {
    const res = await fetch(`https://api.otakugifs.xyz/gif?reaction=${reaction}`);
    if (!res.ok) return null;
    const data = (await res.json()) as { url?: string };
    return data.url ?? null;
  } catch {
    return null;
  }
}
