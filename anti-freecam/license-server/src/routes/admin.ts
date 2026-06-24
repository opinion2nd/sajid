import { FastifyInstance } from 'fastify';
import { requireAdminSecret } from '../middleware/auth';
import { LicenseService } from '../services/LicenseService';

export async function adminRoute(fastify: FastifyInstance) {
  // All admin routes require the admin secret header
  fastify.addHook('preHandler', requireAdminSecret);

  // Generate a new license key
  fastify.post<{
    Body: { product?: string; notes?: string; expiresAt?: string };
  }>('/generate', async (request, reply) => {
    const { product, notes, expiresAt } = request.body ?? {};
    const key = await LicenseService.generate(product ?? 'antifreecam', notes, expiresAt);
    return reply.code(201).send({ key });
  });

  // Revoke a license key
  fastify.post<{ Body: { key: string } }>('/revoke', async (request, reply) => {
    const { key } = request.body;
    const ok = await LicenseService.revoke(key);
    if (!ok) return reply.code(404).send({ error: 'License not found' });
    return reply.code(200).send({ ok: true });
  });

  // Remove server binding (allows key to move to new server)
  fastify.post<{ Body: { key: string } }>('/unbind', async (request, reply) => {
    const { key } = request.body;
    const ok = await LicenseService.unbind(key);
    if (!ok) return reply.code(404).send({ error: 'License not found' });
    return reply.code(200).send({ ok: true });
  });

  // List licenses, optionally filtered by product
  fastify.get<{ Querystring: { product?: string } }>('/licenses', async (request, reply) => {
    const licenses = await LicenseService.listAll(request.query?.product);
    return reply.code(200).send(licenses);
  });

  // Look up a single license by key
  fastify.get<{ Params: { key: string } }>('/licenses/:key', async (request, reply) => {
    const license = await LicenseService.findByKey(request.params.key);
    if (!license) {
      return reply.code(404).send({ error: 'License not found' });
    }
    return reply.code(200).send(license);
  });
}
