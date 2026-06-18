import { MessageSquare, CheckCircle2 } from "lucide-react";
import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";
import { DiscordLinkCode } from "@/components/discord-link";

export const metadata = { title: "Discord" };

export default async function DiscordPage() {
  const user = await requireUser();
  const link = await prisma.discordLink.findUnique({
    where: { userId: user.id },
  });

  return (
    <div>
      <h1 className="text-2xl font-bold">Discord</h1>
      <p className="mt-1 text-muted">
        Link your Discord to get customer roles and receive your license keys in
        your DMs.
      </p>

      <div className="card mt-6 max-w-lg p-6">
        {link ? (
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-8 w-8 text-accent" />
            <div>
              <p className="font-semibold">Linked</p>
              <p className="text-sm text-muted">
                Connected as @{link.discordUsername}
              </p>
            </div>
          </div>
        ) : (
          <>
            <div className="flex items-center gap-2 text-brand">
              <MessageSquare className="h-5 w-5" />
              <span className="font-semibold">Connect your account</span>
            </div>
            <ol className="mt-3 list-decimal space-y-1 pl-5 text-sm text-muted">
              <li>Join the Brother Craft Discord server.</li>
              <li>Generate a one-time code below.</li>
              <li>
                Run <code className="font-mono">/link &lt;code&gt;</code> in any
                channel.
              </li>
            </ol>
            <div className="mt-4">
              <DiscordLinkCode />
            </div>
          </>
        )}
      </div>
    </div>
  );
}
