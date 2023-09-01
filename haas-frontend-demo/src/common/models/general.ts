import { ReactElement } from "react";

export interface INav {
  path: string; // path is a slug of the the route and key for name inside the i18n
  icon: ReactElement;
  template: ReactElement;
  lvl2?: INav[]; // lvl 2
  expanded?: boolean
}

export type TableCell<T> = {
  item: T;
  isFetching: boolean;
};

export type Table<T> = {
  items: T[];
  loading?: boolean;
};

export type THash<T> = {
  [key: string]: T;
};

export type ComponentProps<T> = {
  isActive: boolean;
  onAction?: T | ComponentOnAction;
  loading?: boolean;
};

export type ComponentOnAction = {
  type: string;
  data?: any;
};
