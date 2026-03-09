interface GraphQLResponse<T> {
  data?: T;
  errors?: Array<{ message: string }>;
}

export async function gqlClient<T>(query: string, variables?: Record<string, unknown>): Promise<T> {
  const result = await fetch('/graphql', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ query, variables })
  });

  if (!result.ok) {
    throw new Error(`GraphQL request failed with status ${result.status}`);
  }

  const payload: GraphQLResponse<T> = await result.json();
  if (payload.errors?.length) {
    throw new Error(payload.errors.map((error) => error.message).join(', '));
  }

  if (!payload.data) {
    throw new Error('GraphQL response missing data');
  }

  return payload.data;
}
