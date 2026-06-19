const joinTimestamps = new Map<string, number[]>();

/** Records a join and returns true if the guild's join rate exceeds the raid threshold. */
export function recordJoinAndCheckRaid(guildId: string, thresholdCount: number, windowSeconds: number): boolean {
  const now = Date.now();
  const windowMs = windowSeconds * 1000;
  const timestamps = (joinTimestamps.get(guildId) ?? []).filter((t) => now - t < windowMs);
  timestamps.push(now);
  joinTimestamps.set(guildId, timestamps);
  return timestamps.length >= thresholdCount;
}
