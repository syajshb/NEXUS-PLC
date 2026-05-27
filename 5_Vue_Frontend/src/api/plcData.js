import httpClient from '../utils/http.js'

const PLC_PATH = '/plc-data'

/**
 * @typedef {Object} PlcData
 * @property {number} id
 * @property {string} timestamp
 * @property {number} status
 * @property {number} counter
 * @property {number} temperature
 * @property {number} humidity
 * @property {number} pressure
 * @property {number} powerConsumption
 * @property {number} runHours
 * @property {number} productCount
 * @property {number} qualityRate
 * @property {number} speed
 * @property {number} efficiency
 * @property {number} electricity
 * @property {number} water
 * @property {number} gas
 * @property {number} alarmActive
 * @property {number} alarmLevel
 * @property {number} alarmCounter
 * @property {string} source
 */

/**
 * @typedef {Object} PlcPageResult
 * @property {PlcData[]} items
 * @property {number} total
 * @property {number} page
 * @property {number} size
 */

export const plcApi = {
  health: () => httpClient.get('/health'),

  /** Full list, sorted by id asc */
  listAll: () => httpClient.get(PLC_PATH),

  /**
   * Paginated list
   * @param {number} page 0-based
   * @param {number} size
   * @returns {Promise<PlcPageResult>}
   */
  listPage: (page = 0, size = 20) =>
    httpClient.get(PLC_PATH, { page, size }),

  /** Latest record or null if 404 / empty */
  latest: () => httpClient.getAllow404(`${PLC_PATH}/latest`),

  /** Single record by id or null if 404 */
  getById: (id) => httpClient.getAllow404(`${PLC_PATH}/${id}`)
}
