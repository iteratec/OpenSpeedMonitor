import {TimeSeriesPoint} from './time-series-point.model';

export default class ContextMenuPosition {
  // if divider is set on true, the other fields are ignored
  divider ? = false;

  title?: string;
  icon?: string;
  visible?: (d: TimeSeriesPoint, i: number, elem) => boolean;
  action?: (d: TimeSeriesPoint, i: number, elem) => void;
}
