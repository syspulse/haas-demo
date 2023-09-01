import { Paginator, PaginatorPageState } from "primereact/paginator";
import { Dispatch, ForwardedRef, SetStateAction, forwardRef } from "react";

import "./styles.scss";

const AppTablePagination = forwardRef(
  (
    props: {
      pagination: PaginatorPageState;
      total: number;
      onPageChange: Dispatch<SetStateAction<PaginatorPageState>>;
      pageItems: number;
    },
    ref: ForwardedRef<HTMLDivElement>
  ) => {
    return (
      <div ref={ref} className='appTablePagination'>
        {props.total > 0 && (
          <>
            <p className='text-14 text-w400 text--gray-600 hide-1024'>
              Showing {props.pagination.first === 0 ? 1 : props.pagination.first + 1} -{" "}
              {props.pagination.first + props.pageItems} of {props.total}
            </p>

            <Paginator
              first={props.pagination.first}
              rows={props.pagination.rows}
              totalRecords={props.total}
              onPageChange={props.onPageChange}
            ></Paginator>
          </>
        )}
      </div>
    );
  }
);

export default AppTablePagination;
