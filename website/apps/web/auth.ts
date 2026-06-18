import NextAuth, { type DefaultSession } from "next-auth";
import Credentials from "next-auth/providers/credentials";
import Discord from "next-auth/providers/discord";
import bcrypt from "bcryptjs";
import prisma from "@brothercraft/db";

declare module "next-auth" {
  interface Session {
    user: {
      id: string;
      role: "BUYER" | "SELLER" | "ADMIN";
      handle: string;
    } & DefaultSession["user"];
  }
}

const providers = [
  Credentials({
    name: "Email",
    credentials: {
      email: { label: "Email", type: "email" },
      password: { label: "Password", type: "password" },
    },
    async authorize(creds) {
      const email = String(creds?.email ?? "").toLowerCase();
      const password = String(creds?.password ?? "");
      if (!email || !password) return null;
      const user = await prisma.user.findUnique({ where: { email } });
      if (!user?.passwordHash) return null;
      const ok = await bcrypt.compare(password, user.passwordHash);
      if (!ok) return null;
      return {
        id: user.id,
        email: user.email,
        name: user.handle,
        image: user.avatarUrl ?? undefined,
      };
    },
  }),
];

// Discord OAuth is wired but only active when credentials are present.
if (process.env.AUTH_DISCORD_ID && process.env.AUTH_DISCORD_SECRET) {
  providers.push(Discord as never);
}

export const { handlers, auth, signIn, signOut } = NextAuth({
  trustHost: true,
  session: { strategy: "jwt" },
  pages: { signIn: "/login" },
  providers,
  callbacks: {
    async jwt({ token, user }) {
      // On sign-in, enrich the token; otherwise refresh role/handle from DB.
      const userId = (user?.id as string | undefined) ?? (token.sub as string);
      if (userId) {
        const dbUser = await prisma.user.findUnique({
          where: { id: userId },
          select: { id: true, role: true, handle: true },
        });
        if (dbUser) {
          token.sub = dbUser.id;
          token.role = dbUser.role;
          token.handle = dbUser.handle;
        }
      }
      return token;
    },
    async session({ session, token }) {
      if (session.user) {
        session.user.id = token.sub as string;
        session.user.role = (token.role as "BUYER" | "SELLER" | "ADMIN") ?? "BUYER";
        session.user.handle = (token.handle as string) ?? "";
      }
      return session;
    },
  },
});
