// import React from 'react';
// import { render, waitFor } from '@testing-library/react';
// import LiveGraph from './LiveGraph';
// import fetchMock from 'jest-fetch-mock';
// import { API_URL } from '../constants';
//
// beforeEach(() => {
//   fetchMock.resetMocks();
// });
//
// it('renders the component', async () => {
//   fetchMock.mockResponseOnce(JSON.stringify({ points: [] }));
//   const component = await render(<LiveGraph stockId={1} reloadTrigger={0} />);
//   expect(component).toMatchSnapshot();
// });
//
// it('renders the error message', async () => {
//   fetchMock.mockRejectOnce(new Error('Error'));
//   const component = await render(<LiveGraph stockId={1} reloadTrigger={0} />);
//   await waitFor(() => expect(component.getByText('Error')).toBeInTheDocument());
// });

// TODO: pointless awaits and constant import

import React from 'react';
import { render, waitFor } from '@testing-library/react';
import LiveGraph from './LiveGraph';
import fetchMock from 'jest-fetch-mock';

beforeEach(() => {
  fetchMock.resetMocks();
});

it('renders the component', async () => {
  fetchMock.mockResponseOnce(JSON.stringify({ points: [] }));
  const component = await render(<LiveGraph stockId={1} reloadTrigger={0} />);
  expect(component).toMatchSnapshot();
});

it('renders the error message', async () => {
  fetchMock.mockRejectOnce(new Error('Error'));
  const component = await render(<LiveGraph stockId={1} reloadTrigger={0} />);
  await waitFor(() => expect(component.getByText('Error')).toBeInTheDocument());
});
