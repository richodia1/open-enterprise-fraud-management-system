import { ResourceWorkspace } from '../resources/ResourceWorkspace'
import { listArea } from '../resources/resourceAreas'

export function ListsPage() {
  return <ResourceWorkspace area={listArea} />
}
