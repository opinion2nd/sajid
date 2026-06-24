import { FastifyInstance } from 'fastify';
import { LicenseService } from '../services/LicenseService';

export async function validateRoute(fastify: FastifyInstance) {
  fastify.post<{
    Body: { key: string; serverId: string; pluginVersion: string; product?: string };
  }>('/validate', {
    schema: {
      body: {
        type: 'object',
        required: ['key', 'serverId'],
        properties: {
          key: { type: 'string' },
          serverId: { type: 'string' },
          pluginVersion: { type: 'string' },
          product: { type: 'string' }
        }
      }
    }
  }, async (request, reply) => {
    const { key, serverId, pluginVersion, product } = request.body;
    const ipAddress = request.ip;

    const result = await LicenseService.validate(key, product ?? 'antifreecam', serverId, pluginVersion ?? 'unknown', ipAddress);
    return reply.code(200).send(result);
  });
}
