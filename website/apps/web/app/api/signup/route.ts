import { NextResponse } from "next/server";
import { z } from "zod";
import bcrypt from "bcryptjs";
import prisma from "@brothercraft/db";

const schema = z.object({
  email: z.string().email(),
  handle: z
    .string()
    .min(3)
    .max(20)
    .regex(/^[a-zA-Z0-9_]+$/, "Letters, numbers and underscore only"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

export async function POST(req: Request) {
  let data;
  try {
    data = schema.parse(await req.json());
  } catch (e) {
    const msg =
      e instanceof z.ZodError ? e.errors[0]?.message : "Invalid input";
    return NextResponse.json({ error: msg }, { status: 400 });
  }

  const email = data.email.toLowerCase();
  const exists = await prisma.user.findFirst({
    where: { OR: [{ email }, { handle: data.handle }] },
  });
  if (exists) {
    return NextResponse.json(
      { error: "Email or username already taken" },
      { status: 409 }
    );
  }

  const passwordHash = await bcrypt.hash(data.password, 10);
  await prisma.user.create({
    data: {
      email,
      handle: data.handle,
      passwordHash,
      role: "BUYER",
      wallet: { create: {} },
    },
  });

  return NextResponse.json({ ok: true });
}
