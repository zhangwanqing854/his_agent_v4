import { createProdMockServer } from 'vite-plugin-mock/es/createProdMockServer'
import auth from './auth'
import patient from './patient'
import handover from './handover'

export function setupProdMockServer() {
  createProdMockServer([...auth, ...patient, ...handover])
}
